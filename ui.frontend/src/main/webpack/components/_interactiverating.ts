(function () {
    "use strict";

    const selectors = {
        self:           '[data-cmp-is="interactiverating"]',
        form:           '[data-cmp-hook-interactiverating="form"]',
        thankyou:       '[data-cmp-hook-interactiverating="thankyou"]',
        ratingBtn:      '[data-cmp-hook-interactiverating="ratingBtn"]',
        submit:         '[data-cmp-hook-interactiverating="submit"]',
        selectedRating: '[data-cmp-hook-interactiverating="selectedRating"]',
    };

    const ACTIVE_CLASS = "cmp-interactiverating__rating-btn--active";

    function initComponent(element: HTMLElement): void {
        element.removeAttribute("data-cmp-is");

        const endpoint = element.dataset.submissionEndpoint ?? "";
        let selectedRating = 0;

        const ratingBtns = element.querySelectorAll<HTMLButtonElement>(selectors.ratingBtn);
        const submitBtn  = element.querySelector<HTMLButtonElement>(selectors.submit);

        ratingBtns.forEach((btn) => {
            btn.addEventListener("click", () => {
                ratingBtns.forEach((b) => b.classList.remove(ACTIVE_CLASS));
                btn.classList.add(ACTIVE_CLASS);
                selectedRating = parseInt(btn.dataset.rating ?? "0", 10);
            });
        });

        submitBtn?.addEventListener("click", () => {
            if (selectedRating === 0) return;

            fetch("/libs/granite/csrf/token.json")
                .then((res) => res.json())
                .then((tokenData: { token: string }) =>
                    fetch(endpoint, {
                        method: "POST",
                        headers: {
                            "Content-Type": "application/json",
                            "CSRF-Token": tokenData.token,
                        },
                        body: JSON.stringify({ rating: selectedRating }),
                    })
                )
                .then((res) => res.json())
                .then((data: { success: boolean }) => {
                    if (data.success) {
                        showThankYou(element, selectedRating);
                    }
                })
                .catch((err) => console.error("Rating submission failed:", err));
        });
    }

    function showThankYou(element: HTMLElement, rating: number): void {
        const form     = element.querySelector<HTMLElement>(selectors.form);
        const thankyou = element.querySelector<HTMLElement>(selectors.thankyou);
        const badge    = element.querySelector<HTMLElement>(selectors.selectedRating);

        if (badge) badge.textContent = String(rating);
        if (form)     form.style.display = "none";
        if (thankyou) thankyou.style.display = "block";
    }

    function onDocumentReady(): void {
        document.querySelectorAll<HTMLElement>(selectors.self).forEach(initComponent);

        const observer = new MutationObserver((mutations) => {
            mutations.forEach((mutation) => {
                mutation.addedNodes.forEach((node) => {
                    if (node instanceof HTMLElement && node.querySelectorAll) {
                        node.querySelectorAll<HTMLElement>(selectors.self).forEach(initComponent);
                    }
                });
            });
        });

        observer.observe(document.body, { subtree: true, childList: true, characterData: true });
    }

    if (document.readyState !== "loading") {
        onDocumentReady();
    } else {
        document.addEventListener("DOMContentLoaded", onDocumentReady);
    }
})();
