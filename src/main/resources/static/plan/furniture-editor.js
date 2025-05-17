class FurnitureEditor extends PlanViewer {
    constructor(app, wallNodes, spacerNodes, itemNodes, setItems, planId) {
        super(app, wallNodes, spacerNodes, itemNodes, setItems);

        this.planId = planId;
        this.furnitureItems = [];
        this.selectedItem = null;
        this.isDragging = false;
        this.lastValidPosition = {x: 0, y: 0};

        this.initUIElements();
        this.drawObjects();
        this.setupEventListeners();
        this.setupFurnitureList();
    }

    // defer drawObjects(), PlanViewer calls it
    // but it's doing so before furnitureItems is initialized
    // overridden addFurnitureItem is using that list
    // this does seem like a code smell, but I'm not sure how to fix it yet
    drawObjects() {
        if (!this.furnitureItems) {
            return;
        }

        super.drawObjects();
    }

    initUIElements() {
        this.rotationSlider = document.getElementById('rotation-slider');
        this.rotationValue = document.getElementById('rotation-value');
        this.rotationPanel = document.getElementById('rotation-slider-panel');
        this.selectedItemText = document.getElementById('selected-item');
        this.positionInfo = document.getElementById('position-info');
        this.saveButton = document.getElementById('save-btn');
        this.deletionButton = document.getElementById('deletion-btn');
        this.triangleAlert = document.getElementById('triangle-alert');
        this.objectAlert = document.getElementById('object-alert');
        this.spacerBtn = document.getElementById('add-spacer');
        this.initSpacerModal();
    }

    initSpacerModal(){
        this.addSpacerConfirmBtn = document.getElementById("add-spacer-confirm");
        this.spacerWidthInput = document.getElementById("spacer-width");
        this.spacerHeightInput = document.getElementById("spacer-height");
        this.spacerAlert = document.getElementById("spacer-alert");
        const spacerModal = document.getElementById("spacer-modal");
        this.spacerModalInstance = new bootstrap.Modal(spacerModal);
    }

    showSpacerInputForm() {
        this.spacerAlert.style.display = 'none';
        this.spacerWidthInput.value = "50";
        this.spacerHeightInput.value = "50";
        this.spacerModalInstance.show();
    }

    setupEventListeners() {
        // rotation slider events
        this.rotationSlider.addEventListener('input', () => {
            if (this.selectedItem) {
                const rotation = parseInt(this.rotationSlider.value);
                this.rotateObject(this.selectedItem, rotation);
                this.rotationValue.textContent = `${rotation}°`;
            }
        });

        // spacer button
        this.spacerBtn.addEventListener('click', () => {
            this.showSpacerInputForm();
        });

        this.addSpacerConfirmBtn.addEventListener('click', () => {
            this.addSpacer();
        })

        // save button
        this.saveButton.addEventListener('click', () => {
            this.savePlan();
        });

        this.deletionButton.addEventListener('click', () => {
            this.deleteObject();
        })
    }

    deleteObject() {
        this.furnitureContainer.removeChild(this.selectedItem);
        this.furnitureItems = this.furnitureItems.filter(item => item !== this.selectedItem);
        this.selectedItem = null;
        this.rotationPanel.style.display = 'none';
    }

    setupFurnitureList() {
        const furnitureListItems = document.querySelectorAll('.furniture-item');

        furnitureListItems.forEach(item => {
            item.addEventListener('click', () => {
                const width = parseInt(item.getAttribute('data-width'));
                const height = parseInt(item.getAttribute('data-height'));
                const id = parseInt(item.getAttribute('data-id'));
                const name = item.querySelector('strong').textContent;
                const centerPosition = this.calculateRoomCenter();

                this.objectAlert.style.display = 'none';

                const added = this.addFurnitureItem({
                    x: centerPosition.x,
                    y: centerPosition.y,
                    id: id,
                    name: name,
                    width: width,
                    height: height,
                    color: this.stringToColor(name)
                });

                if (!added) {
                    this.objectAlert.style.display = "block";
                    this.objectAlert.textContent = `${name} doesn't fit into the room`
                }
            });
        });
    }

    addFurnitureItem(itemData) {
        const furniture = this.createFurnitureItem(itemData);
        furniture.id = itemData.id || -1;
        furniture.label = itemData.name;
        furniture.originalWidth = itemData.width;
        furniture.originalHeight = itemData.height;
        furniture.eventMode = 'static';
        furniture.cursor = 'pointer';

        this.setupDraggableItem(furniture);

        if (!this.isInsideRoom(furniture)) {
            return false;
        }

        this.furnitureContainer.addChild(furniture);
        this.furnitureItems.push(furniture);

        this.selectObject(furniture);

        return true;
    }

    setupDraggableItem(item) {
        item
            .on('pointerdown', this.onDragStart.bind(this, item))
            .on('pointerup', this.onDragEnd.bind(this, item))
            .on('pointerupoutside', this.onDragEnd.bind(this, item))
            .on('pointermove', this.onDragMove.bind(this, item))
            .on('click', () => this.selectObject(item));
    }

    onDragStart(item, event) {
        if (!this.selectedItem || this.selectedItem !== item) {
            this.selectObject(item);
        }

        this.triangleAlert.textContent = "";
        this.triangleAlert.style.display = 'none';

        this.isDragging = true;
        item.alpha = 0.8;
        item.dragging = true;

        this.lastValidPosition = {x: item.x, y: item.y};
    }

    onDragEnd(item, event) {
        this.isDragging = false;
        item.alpha = 1;
        item.dragging = false;

        // if not inside room, revert
        if (!this.isInsideRoom(item)) {
            item.position.set(this.lastValidPosition.x, this.lastValidPosition.y);
        }

        this.updatePositionInfo(item);

        const triangleCheck = this.checkKitchenTriangle();
        if (!triangleCheck.valid) {
            this.showTriangleIssues(triangleCheck.issues);
        }
    }

    showTriangleIssues(issues) {
        let html = "Kitchen triangle violations: <ul>";

        for (const issue of issues) {
            html += `<li>${issue}</li>`
        }

        html += "</ul>";

        this.triangleAlert.innerHTML = html;
        this.triangleAlert.style.display = 'block';
    }

    onDragMove(item, event) {
        if (item.dragging) {
            const newPosition = event.global;
            const localPos = this.furnitureContainer.toLocal(newPosition);

            const oldX = item.x;
            const oldY = item.y;

            item.position.set(localPos.x, localPos.y);

            if (this.checkCollisions(item)) {
                item.position.set(oldX, oldY);
            } else {
                this.lastValidPosition = {x: item.x, y: item.y};
            }

            this.updatePositionInfo(item);
        }
    }

    updatePositionInfo(item) {
        this.positionInfo.textContent = `Position: X: ${Math.round(item.x)} Y: ${Math.round(item.y)}`;
    }

    showObjectControls(item){
        this.rotationPanel.style.display = 'block';
        this.rotationSlider.value = (item.rotation * 180 / Math.PI) % 360;
        this.rotationValue.textContent = `${Math.round((item.rotation * 180 / Math.PI) % 360)}°`;
        this.selectedItemText.textContent = `Selected: ${item.name}`;
    }

    selectObject(item) {
        if (this.selectedItem) {
            this.selectedItem.tint = 0xFFFFFF;
        }

        this.selectedItem = item;
        this.selectedItem.tint = 0xFFCC00;

        // update ui
        this.showObjectControls(item);
        this.updatePositionInfo(item);
    }

    rotateObject(item, degrees) {
        item.rotation = MathUtils.degreesToRadians(degrees);

        if (!this.isInsideRoom(item)) {
            this.pushObjectIntoBounds(item);
        }
    }

    pushObjectIntoBounds(item) {
        const roomBounds = this.room.getBounds();

        const itemBounds = item.getBounds();

        let xAdjust = 0;
        let yAdjust = 0;

        // x-axis adjustment
        if (itemBounds.left < roomBounds.left) {
            xAdjust = roomBounds.left - itemBounds.left;
        } else if (itemBounds.right > roomBounds.right) {
            xAdjust = roomBounds.right - itemBounds.right;
        }

        // y-axis adjustment
        if (itemBounds.top < roomBounds.top) {
            yAdjust = roomBounds.top - itemBounds.top;
        } else if (itemBounds.bottom > roomBounds.bottom) {
            yAdjust = roomBounds.bottom - itemBounds.bottom;
        }

        item.position.set(item.x + xAdjust, item.y + yAdjust);
    }

    // FIXME: collisions feel "sticky", makes it hard to move the object
    checkCollisions(item) {
        if (!this.isInsideRoom(item)) {
            return true;
        }

        for (const other of this.furnitureItems) {
            // skip collision check with itself
            if (other !== item) {
                const itemBounds = item.getBounds();
                const otherBounds = other.getBounds();

                if (this.boundsIntersect(itemBounds, otherBounds)) {
                    return true;
                }
            }
        }
        return false;
    }

    // heuristic algorithm for checking kitchen triangle rule
    checkKitchenTriangle() {
        const fridge = this.furnitureItems.find(item => {
            const label = item.label.toLowerCase();

            return label.indexOf("fridge") !== -1 ||
                label.indexOf("refrigerator") !== -1;
        });
        if (!fridge) {
            return {valid: true};
        }

        const stove = this.furnitureItems.find(item => {
                const label = item.label.toLowerCase();
                return label.indexOf("stove") !== -1 ||
                    label.indexOf("range") !== -1 ||
                    label.indexOf("cooktop") !== -1
            }
        );
        if (!stove) {
            return {valid: true};
        }

        const sink = this.furnitureItems.find(item => item.label.toLowerCase().indexOf("sink") !== -1);
        if (!sink) {
            return {valid: true};
        }

        const fridgeCenter = {
            x: fridge.x + fridge.width / 2,
            y: fridge.y + fridge.height / 2
        };
        const stoveCenter = {
            x: stove.x + stove.width / 2,
            y: stove.y + stove.height / 2
        };
        const sinkCenter = {
            x: sink.x + sink.width / 2,
            y: sink.y + sink.height / 2
        };

        const fridgeToStove = MathUtils.getDistance(fridgeCenter, stoveCenter);
        const fridgeToSink = MathUtils.getDistance(fridgeCenter, sinkCenter);
        const stoveToSink = MathUtils.getDistance(stoveCenter, sinkCenter);

        const perimeter = fridgeToStove + fridgeToSink + stoveToSink;

        // source: https://en.wikipedia.org/wiki/Kitchen_work_triangle#Application
        const minLegDistance = 120;
        const maxLegDistance = 270;
        const minPerimeter = 400;
        const maxPerimeter = 800;

        const issues = [];

        // check each triangle leg distance
        if (fridgeToStove < minLegDistance) {
            issues.push(`Refrigerator to stove distance (${fridgeToStove.toFixed(1)}) is too small (min: ${minLegDistance})`);
        } else if (fridgeToStove > maxLegDistance) {
            issues.push(`Refrigerator to stove distance (${fridgeToStove.toFixed(1)}) is too large (max: ${maxLegDistance})`);
        }

        if (fridgeToSink < minLegDistance) {
            issues.push(`Refrigerator to sink distance (${fridgeToSink.toFixed(1)}) is too small (min: ${minLegDistance})`);
        } else if (fridgeToSink > maxLegDistance) {
            issues.push(`Refrigerator to sink distance (${fridgeToSink.toFixed(1)}) is too large (max: ${maxLegDistance})`);
        }

        if (stoveToSink < minLegDistance) {
            issues.push(`Stove to sink distance (${stoveToSink.toFixed(1)}) is too small (min: ${minLegDistance})`);
        } else if (stoveToSink > maxLegDistance) {
            issues.push(`Stove to sink distance (${stoveToSink.toFixed(1)}) is too large (max: ${maxLegDistance})`);
        }

        // check triangle perimeter
        if (perimeter < minPerimeter) {
            issues.push(`Triangle perimeter (${perimeter.toFixed(1)}) is too small (min: ${minPerimeter})`);
        } else if (perimeter > maxPerimeter) {
            issues.push(`Triangle perimeter (${perimeter.toFixed(1)}) is too large (max: ${maxPerimeter})`);
        }

        return {
            valid: issues.length === 0,
            issues,
            measurements: {
                fridgeToStove,
                fridgeToSink,
                stoveToSink,
                perimeter
            }
        };
    }

    boundsIntersect(bounds1, bounds2) {
        return !(
            bounds1.right < bounds2.left ||
            bounds1.left > bounds2.right ||
            bounds1.bottom < bounds2.top ||
            bounds1.top > bounds2.bottom
        );
    }

    isInsideRoom(item) {
        const roomPolygon = this.getRoomPolyCoords();

        const itemBounds = item.getBounds();

        const itemPoints = [
            {x: itemBounds.left, y: itemBounds.top},
            {x: itemBounds.right, y: itemBounds.top},
            {x: itemBounds.right, y: itemBounds.bottom},
            {x: itemBounds.left, y: itemBounds.bottom}
        ];

        for (const point of itemPoints) {
            if (!this.isPointInPolygon(point, roomPolygon)) {
                return false;
            }
        }

        return true;
    }

    // gets new polygon coordinates after panning or zooming
    getRoomPolyCoords() {
        const globalPolygon = [];

        for (const node of this.wallNodes) {
            const globalPoint = this.room.toGlobal(new PIXI.Point(node.x, node.y));
            globalPolygon.push({x: globalPoint.x, y: globalPoint.y});
        }

        return globalPolygon;
    }

    isPointInPolygon(point, polygon) {
        // ray casting (even-odd) algorithm
        let inside = false;
        for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
            const xi = polygon[i].x;
            const yi = polygon[i].y;
            const xj = polygon[j].x;
            const yj = polygon[j].y;

            const intersect = ((yi > point.y) !== (yj > point.y)) &&
                (point.x < (xj - xi) * (point.y - yi) / (yj - yi) + xi);

            if (intersect) inside = !inside;
        }

        return inside;
    }

    calculateRoomCenter() {
        const bounds = this.room.getBounds();
        return {
            x: (bounds.left + bounds.right) / 2,
            y: (bounds.top + bounds.bottom) / 2
        };
    }

    addSpacer() {
        const width = parseInt(this.spacerWidthInput.value);
        const height = parseInt(this.spacerHeightInput.value);

        if (isNaN(width) || width <= 0 || height <= 0) {
            this.spacerAlert.style.display = "block";
            this.spacerAlert.textContent = "Width and height cannot be negative"
            return;
        }

        const centerPosition = this.calculateRoomCenter();

        const added = this.addFurnitureItem({
            x: centerPosition.x,
            y: centerPosition.y,
            name: "Spacer",
            width: width,
            height: height,
            color: this.SPACER_COLOR
        });

        if (!added) {
            this.spacerAlert.style.display = "block";
            this.spacerAlert.textContent = "Spacer is too large to fit into the room"
            return;
        }

        this.spacerModalInstance.hide();
    }

    async savePlan() {
        const spacers = this.furnitureItems.map(item => {
            if (item.label === "Spacer") {
                return {
                    x: item.x,
                    y: item.y,
                    angle: Math.round(item.rotation),
                    width: item.originalWidth,
                    height: item.originalHeight
                };
            }
        })

        const items = this.furnitureItems.map(item => {
            if (item.label !== "Spacer") {
                return {
                    id: item.id,
                    x: item.x,
                    y: item.y,
                    angle: Math.round(item.rotation)
                }
            }
        })

        try {
            const response = await fetch(`/plans/edit/${this.planId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    spacers,
                    items
                })
            });

            if (response.ok) {
                const data = await response.json();
                window.location.href = data.redirect;
            }
        } catch (error) {
            console.error("Error saving plan:", error);
            alert("An error occurred while saving furniture plan.");
        }

    }
}
