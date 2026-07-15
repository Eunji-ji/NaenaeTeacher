document.addEventListener('DOMContentLoaded', () => {
    const sidebar = document.querySelector('.dashboard-sidebar');
    const header = document.querySelector('.dashboard-header');
    if (!sidebar || !header) return;

    sidebar.setAttribute('id', 'dashboardSidebar');

    const openButton = document.createElement('button');
    openButton.type = 'button';
    openButton.className = 'mobile-menu-button';
    openButton.setAttribute('aria-label', '메뉴 열기');
    openButton.setAttribute('aria-controls', 'dashboardSidebar');
    openButton.setAttribute('aria-expanded', 'false');
    openButton.innerHTML = '<span aria-hidden="true">☰</span>';
    header.insertBefore(openButton, header.firstChild);

    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.className = 'mobile-sidebar-close';
    closeButton.setAttribute('aria-label', '메뉴 닫기');
    closeButton.innerHTML = '<span aria-hidden="true">×</span>';
    sidebar.appendChild(closeButton);

    const overlay = document.createElement('button');
    overlay.type = 'button';
    overlay.className = 'mobile-sidebar-overlay';
    overlay.setAttribute('aria-label', '메뉴 닫기');
    document.body.appendChild(overlay);

    const open = () => {
        sidebar.classList.add('is-open');
        overlay.classList.add('is-open');
        document.body.classList.add('mobile-nav-open');
        openButton.setAttribute('aria-expanded', 'true');
        closeButton.focus();
    };
    const close = () => {
        sidebar.classList.remove('is-open');
        overlay.classList.remove('is-open');
        document.body.classList.remove('mobile-nav-open');
        openButton.setAttribute('aria-expanded', 'false');
    };

    openButton.addEventListener('click', open);
    closeButton.addEventListener('click', close);
    overlay.addEventListener('click', close);
    sidebar.querySelectorAll('a').forEach(link => link.addEventListener('click', close));
    document.addEventListener('keydown', event => {
        if (event.key === 'Escape' && sidebar.classList.contains('is-open')) {
            close();
            openButton.focus();
        }
    });
    window.addEventListener('resize', () => {
        if (window.innerWidth > 860) close();
    });
});