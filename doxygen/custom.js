(function () {
  function sideNav() {
    return document.getElementById('side-nav') || document.getElementById('nav-tree');
  }

  function updateNavWidth() {
    var nav = sideNav();
    var width = nav ? Math.max(nav.getBoundingClientRect().width, 44) : 44;
    document.documentElement.style.setProperty('--insight-nav-width', width + 'px');
  }

  function setCollapsed(collapsed) {
    document.body.classList.toggle('insight-nav-collapsed', collapsed);
    var button = document.getElementById('insight-nav-toggle');
    if (button) {
      button.textContent = collapsed ? 'Show navigation' : 'Hide navigation';
      button.setAttribute('aria-expanded', String(!collapsed));
    }
    updateNavWidth();
  }

  function installToggle() {
    if (document.getElementById('insight-nav-toggle')) {
      return;
    }
    var button = document.createElement('button');
    button.id = 'insight-nav-toggle';
    button.type = 'button';
    button.textContent = 'Hide navigation';
    button.setAttribute('aria-controls', 'side-nav');
    button.setAttribute('aria-expanded', 'true');
    button.addEventListener('click', function () {
      setCollapsed(!document.body.classList.contains('insight-nav-collapsed'));
    });
    document.body.appendChild(button);
    updateNavWidth();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', installToggle);
  } else {
    installToggle();
  }
  window.addEventListener('resize', updateNavWidth);
  if (window.ResizeObserver) {
    var observer = new ResizeObserver(updateNavWidth);
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', function () {
        var nav = sideNav();
        if (nav) {
          observer.observe(nav);
        }
      });
    } else {
      var nav = sideNav();
      if (nav) {
        observer.observe(nav);
      }
    }
  }
}());
