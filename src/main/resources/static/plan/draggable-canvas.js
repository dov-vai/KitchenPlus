class DraggableCanvas extends PIXI.Container {
    constructor(app, options = {}) {
        super();
        this.app = app;

        this.options = {
            minScale: 0.3,
            maxScale: 1.5,
            zoomFactor: 1.2,
            ...options
        };

        this.setupCanvas();
    }

    setupCanvas() {
        this.eventMode = "static";
        this.hitArea = this.app.screen;

        this.isPanning = false;
        this.lastPanPosition = null;

        this.setupZooming();
        this.setupPanning();

        this.app.canvas.style.cursor = "grab";

        // block right click menu
        this.app.canvas.addEventListener("contextmenu", (e) => {
            e.preventDefault();
        });
    }

    setupZooming() {
        this.app.canvas.addEventListener("wheel", (e) => {
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
        this.on("pointerdown", this.onPointerDown.bind(this));
        this.on("pointermove", this.onPointerMove.bind(this));
        this.on("pointerup", this.onPointerUpOrLeave.bind(this));
        this.on("pointerupoutside", this.onPointerUpOrLeave.bind(this));
        this.on("pointerout", this.onPointerUpOrLeave.bind(this));
    }

    onPointerDown(e) {
        // right mouse button
        if (e.data.button === 2) {
            this.isPanning = true;
            this.lastPanPosition = e.data.global.clone();
            this.app.canvas.style.cursor = "grabbing";
            e.stopPropagation();
        }
    }

    onPointerMove(e) {
        if (this.isPanning) {
            const newPos = e.data.global;
            const dx = newPos.x - this.lastPanPosition.x;
            const dy = newPos.y - this.lastPanPosition.y;
            this.x += dx;
            this.y += dy;
            this.lastPanPosition = newPos.clone();
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