/**
 * Renders every Cloudinary 3D model on the page with the Product Gallery widget.
 *
 * The view emits one container per node and carries the widget options on it, so this script is
 * loaded once per page whatever the number of models. The container gets its id here rather than
 * from the view, because a node identifier is not necessarily a usable CSS selector.
 */
(function () {
    'use strict';

    var READY_RETRIES = 100;
    var READY_DELAY_MS = 50;

    function render(container, index) {
        if (container.getAttribute('data-cloudy-rendered')) {
            return;
        }

        container.setAttribute('data-cloudy-rendered', 'true');
        if (!container.id) {
            container.id = 'cloudy-model3d-' + index;
        }

        const rendering = window.cloudinary.galleryWidget({
            container: '#' + container.id,
            cloudName: container.getAttribute('data-cloud-name'),
            aspectRatio: container.getAttribute('data-aspect-ratio'),
            mediaAssets: [{publicId: container.getAttribute('data-public-id'), mediaType: '3d'}],
            ar3dProps: {
                showAR: container.getAttribute('data-show-ar') === 'true',
                autoRotate: container.getAttribute('data-auto-rotate') === 'true'
            }
        }).render();

        // A plan without the Product Gallery, or an asset it cannot display, rejects here and leaves
        // an empty container. Name the way out, since the container is already marked as rendered.
        if (rendering && typeof rendering.catch === 'function') {
            rendering.catch(function (error) {
                console.error('Cloudinary Product Gallery could not render ' +
                    container.getAttribute('data-public-id') +
                    ', render the model with the "viewer" view instead.', error);
            });
        }
    }

    function renderAll(retriesLeft) {
        var containers = document.querySelectorAll('.cloudy-model3d');
        if (containers.length === 0) {
            return;
        }

        if (!window.cloudinary || !window.cloudinary.galleryWidget) {
            if (retriesLeft > 0) {
                window.setTimeout(function () {
                    renderAll(retriesLeft - 1);
                }, READY_DELAY_MS);
            } else {
                console.error('Cloudinary Product Gallery did not load, 3D models are not rendered.');
            }

            return;
        }

        Array.prototype.forEach.call(containers, render);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            renderAll(READY_RETRIES);
        });
    } else {
        renderAll(READY_RETRIES);
    }
})();
