class DraggableCanvas extends PIXI.Container {
    constructor(app, options = {}) {
        super();
        this.app = app;
        this.eventElement = this.app.renderer.events.domElement;

        this.options = {
            minScale: 0.3,
            maxScale: 1.5,
            zoomFactor: 1.2,
            ...options
        };

        this.setupCanvas();
    }

    setupCanvas() {
        this.isPanning = false;
        this.lastPanPosition = null;

        this.setupZooming();
        this.setupPanning();

        this.app.canvas.style.cursor = "grab";
    }

    setupZooming() {
        this.eventElement.addEventListener("wheel", (e) => {
            e.preventDefault();
            const scaleFactor = e.deltaY < 0 ? this.options.zoomFactor : 1 / this.options.zoomFactor;
            this.applyScale(scaleFactor, e.clientX, e.clientY);
        });
    }

    applyScale(scaleFactor, x, y) {
        const worldPos = new PIXI.Point(
            (x - this.x) / this.scale.x,
            (y - this.y) / this.scale.y
        );

        const newScale = new PIXI.Point(
            this.scale.x * scaleFactor,
            this.scale.y * scaleFactor
        );

        // limit scale so user doesn"t lose the world
        if (newScale.x > this.options.maxScale ||
            newScale.x < this.options.minScale ||
            newScale.y > this.options.maxScale ||
            newScale.y < this.options.minScale) {
            return;
        }

        // apply zoom, convert new world coordinates back to screen coordinates
        let newScreenPos = new PIXI.Point(
            worldPos.x * newScale.x + this.x,
            worldPos.y * newScale.y + this.y
        );

        // adjust the difference after zooming based on pointer location
        // (mouse pointer before zoom) - (after zoom)
        this.x += x - newScreenPos.x;
        this.y += y - newScreenPos.y;
        this.scale.x = newScale.x;
        this.scale.y = newScale.y;
    }

    setupPanning() {
        this.eventElement.addEventListener("contextmenu", this.onPointerDown.bind(this));
        this.eventElement.addEventListener("pointermove", this.onPointerMove.bind(this));
        this.eventElement.addEventListener("pointerup", this.onPointerUpOrLeave.bind(this));
        this.eventElement.addEventListener("pointerupoutside", this.onPointerUpOrLeave.bind(this));
        this.eventElement.addEventListener("pointerout", this.onPointerUpOrLeave.bind(this));
    }

    onPointerDown(e) {
        e.preventDefault();

        this.isPanning = true;
        this.lastPanPosition = {x: e.clientX, y: e.clientY};
        this.app.canvas.style.cursor = "grabbing";
    }

    onPointerMove(e) {
        if (this.isPanning) {
            const newPos = {x: e.clientX, y: e.clientY};
            const dx = newPos.x - this.lastPanPosition.x;
            const dy = newPos.y - this.lastPanPosition.y;
            this.x += dx;
            this.y += dy;
            this.lastPanPosition = {...newPos};
        }
    }

    onPointerUpOrLeave() {
        if (this.isPanning) {
            this.isPanning = false;
            this.lastPanPosition = null;
            this.app.canvas.style.cursor = "grab";
        }
    }
}