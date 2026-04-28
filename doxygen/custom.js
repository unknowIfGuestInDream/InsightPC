(function () {
    const PACKAGE_MENU_TEXT = "Packages";
    const PACKAGE_LIST_MENU_TEXT = "Package List";
    const GENERATED_PACKAGE_LIST = "namespaces.html";
    const PACKAGE_DOCS_FILE = "packages_8dox.html";

    function addSidebarToggle() {
        const sideNav = document.getElementById("side-nav");
        const docContent = document.getElementById("doc-content");
        if (!sideNav || !docContent || document.getElementById("sidebar-toggle")) {
            return;
        }

        const button = document.createElement("button");
        button.id = "sidebar-toggle";
        button.type = "button";

        const DEFAULT_BUTTON_WIDTH = 140;
        const BUTTON_GAP = 14;
        const MINIMUM_LEFT_POSITION = 18;
        const updatePosition = () => {
            if (document.body.classList.contains("sidebar-collapsed")) {
                button.style.left = `${MINIMUM_LEFT_POSITION}px`;
                return;
            }
            const sideNavWidth = sideNav.getBoundingClientRect().width;
            const buttonWidth = button.getBoundingClientRect().width || DEFAULT_BUTTON_WIDTH;
            if (sideNavWidth < buttonWidth + (BUTTON_GAP * 2)) {
                button.style.left = `${MINIMUM_LEFT_POSITION}px`;
                return;
            }
            button.style.left = `${Math.max(MINIMUM_LEFT_POSITION, sideNavWidth - buttonWidth - BUTTON_GAP)}px`;
        };

        const setState = (collapsed) => {
            document.body.classList.toggle("sidebar-collapsed", collapsed);
            button.textContent = collapsed ? "Show navigation" : "Hide navigation";
            button.setAttribute("aria-expanded", String(!collapsed));
            window.requestAnimationFrame(updatePosition);
            try {
                window.localStorage.setItem("doxygenSidebarCollapsed", collapsed ? "true" : "false");
            } catch (e) {
                // Ignore storage restrictions in local file previews.
            }
        };

        button.addEventListener("click", () => setState(!document.body.classList.contains("sidebar-collapsed")));
        document.body.appendChild(button);

        let collapsed;
        try {
            collapsed = window.localStorage.getItem("doxygenSidebarCollapsed") === "true";
        } catch (e) {
            collapsed = false;
        }
        setState(collapsed);
        window.addEventListener("resize", updatePosition);

        if (window.doxygenSidebarToggleObserver) {
            window.doxygenSidebarToggleObserver.disconnect();
        }
        if (typeof ResizeObserver !== "undefined") {
            window.doxygenSidebarToggleObserver = new ResizeObserver(updatePosition);
            window.doxygenSidebarToggleObserver.observe(sideNav);
        }
    }

    function pointPackageMenuToPackageDocs() {
        document
            .querySelectorAll(`a[href="${GENERATED_PACKAGE_LIST}"], a[href$="/${GENERATED_PACKAGE_LIST}"]`)
            .forEach((link) => {
                const text = link.textContent.trim();
                if (text === PACKAGE_MENU_TEXT || text === PACKAGE_LIST_MENU_TEXT) {
                    link.setAttribute("href", PACKAGE_DOCS_FILE);
                }
            });
    }

    document.addEventListener("DOMContentLoaded", () => {
        pointPackageMenuToPackageDocs();
        addSidebarToggle();
    });
}());
