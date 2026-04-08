// --- Types ---

type Model = Readonly<{
    rating: number;
    submitted: boolean;
    loading: boolean;
    endpoint: string;
}>;

type Msg =
    | { readonly type: "select"; readonly rating: number }
    | { readonly type: "submit" }
    | { readonly type: "csrfReceived"; readonly token: string }
    | { readonly type: "submitSucceeded" }
    | { readonly type: "submitFailed"; readonly error: unknown };

type Cmd =
    | { readonly type: "none" }
    | { readonly type: "fetchCsrf" }
    | { readonly type: "submitRating"; readonly endpoint: string; readonly rating: number; readonly token: string };

type ViewState = Readonly<{
    activeRating: number;
    formVisible: boolean;
    thankyouVisible: boolean;
    badgeText: string;
    submitDisabled: boolean;
}>;

type DOMRefs = Readonly<{
    buttons: readonly HTMLButtonElement[];
    submitBtn?: HTMLButtonElement;
    form?: HTMLElement;
    thankyou?: HTMLElement;
    badge?: HTMLElement;
}>;

// --- Constants ---

const selectors = {
    self:           '[data-cmp-is="interactiverating"]',
    form:           '[data-cmp-hook-interactiverating="form"]',
    thankyou:       '[data-cmp-hook-interactiverating="thankyou"]',
    ratingBtn:      '[data-cmp-hook-interactiverating="ratingBtn"]',
    submit:         '[data-cmp-hook-interactiverating="submit"]',
    selectedRating: '[data-cmp-hook-interactiverating="selectedRating"]',
} as const;

const ACTIVE_CLASS = "cmp-interactiverating__rating-btn--active";
const CSRF_URL = "/libs/granite/csrf/token.json";

// --- Pure: init ---

const init = (endpoint: string): [Model, Cmd] => [
    { rating: 0, submitted: false, loading: false, endpoint },
    { type: "none" },
];

// --- Pure: update ---

const update = (msg: Msg, model: Model): [Model, Cmd] => {
    switch (msg.type) {
        case "select":
            return [{ ...model, rating: msg.rating }, { type: "none" }];
        case "submit":
            if (model.rating === 0 || model.loading) return [model, { type: "none" }];
            return [{ ...model, loading: true }, { type: "fetchCsrf" }];
        case "csrfReceived":
            return [model, { type: "submitRating", endpoint: model.endpoint, rating: model.rating, token: msg.token }];
        case "submitSucceeded":
            return [{ ...model, submitted: true, loading: false }, { type: "none" }];
        case "submitFailed":
            return [{ ...model, loading: false }, { type: "none" }];
    }
};

// --- Pure: view ---

const view = (model: Model): ViewState => ({
    activeRating: model.submitted ? 0 : model.rating,
    formVisible: !model.submitted,
    thankyouVisible: model.submitted,
    badgeText: String(model.rating),
    submitDisabled: model.loading,
});

// --- Impure: render ---

const render = (viewState: ViewState, refs: DOMRefs): void => {
    refs.buttons.forEach((btn) =>
        btn.classList.toggle(ACTIVE_CLASS, parseInt(btn.dataset.rating ?? "0", 10) === viewState.activeRating)
    );
    if (refs.submitBtn) refs.submitBtn.disabled = viewState.submitDisabled;
    if (refs.badge)     refs.badge.textContent = viewState.badgeText;
    if (refs.form)      refs.form.style.display = viewState.formVisible ? "" : "none";
    if (refs.thankyou)  refs.thankyou.style.display = viewState.thankyouVisible ? "block" : "none";
};

// --- Impure: command executor ---

const executeCmd = (cmd: Cmd, dispatch: (msg: Msg) => void): void => {
    switch (cmd.type) {
        case "none":
            break;
        case "fetchCsrf":
            fetch(CSRF_URL)
                .then((res) => res.json())
                .then((data: { token: string }) => dispatch({ type: "csrfReceived", token: data.token }))
                .catch((error) => dispatch({ type: "submitFailed", error }));
            break;
        case "submitRating":
            fetch(cmd.endpoint, {
                method: "POST",
                headers: { "Content-Type": "application/json", "CSRF-Token": cmd.token },
                body: JSON.stringify({ rating: cmd.rating }),
            })
                .then((res) => res.json())
                .then((data: { success: boolean }) =>
                    dispatch(data.success
                        ? { type: "submitSucceeded" }
                        : { type: "submitFailed", error: "Server rejected submission" })
                )
                .catch((error) => dispatch({ type: "submitFailed", error }));
            break;
    }
};

// --- Impure: query DOM refs once ---

const queryRefs = (element: HTMLElement): DOMRefs => ({
    buttons:   Array.from(element.querySelectorAll<HTMLButtonElement>(selectors.ratingBtn)),
    submitBtn: element.querySelector<HTMLButtonElement>(selectors.submit) ?? undefined,
    form:      element.querySelector<HTMLElement>(selectors.form) ?? undefined,
    thankyou:  element.querySelector<HTMLElement>(selectors.thankyou) ?? undefined,
    badge:     element.querySelector<HTMLElement>(selectors.selectedRating) ?? undefined,
});

// --- Runtime ---

const mount = (element: HTMLElement): void => {
    element.removeAttribute("data-cmp-is");

    const refs = queryRefs(element);
    const [initialModel, initialCmd] = init(element.dataset.submissionEndpoint ?? "");
    let model = initialModel;

    const dispatch = (msg: Msg): void => {
        const [nextModel, cmd] = update(msg, model);
        model = nextModel;
        render(view(model), refs);
        executeCmd(cmd, dispatch);
    };

    // Subscriptions: DOM events only produce messages
    refs.buttons.forEach((btn) =>
        btn.addEventListener("click", () =>
            dispatch({ type: "select", rating: parseInt(btn.dataset.rating ?? "0", 10) })
        )
    );

    refs.submitBtn?.addEventListener("click", () => dispatch({ type: "submit" }));

    // Initial render + command
    render(view(model), refs);
    executeCmd(initialCmd, dispatch);
};

// --- Bootstrap ---

const initUnmounted = (root: ParentNode): void =>
    root.querySelectorAll<HTMLElement>(selectors.self).forEach(mount);

const onDocumentReady = (): void => {
    initUnmounted(document);

    new MutationObserver((mutations) =>
        mutations
            .flatMap((m) => Array.from(m.addedNodes))
            .filter((node): node is HTMLElement => node instanceof HTMLElement)
            .forEach(initUnmounted)
    ).observe(document.body, { subtree: true, childList: true });
};

if (document.readyState !== "loading") {
    onDocumentReady();
} else {
    document.addEventListener("DOMContentLoaded", onDocumentReady);
}
