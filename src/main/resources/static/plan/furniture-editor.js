class FurnitureEditor {
    constructor(app, containerElementId, planId, wallNodes, spacerNodes, itemNodes, setItems) {
        this.app = app;
        this.containerElement = document.getElementById(containerElementId);
        this.planId = planId;
        this.spacerNodes = spacerNodes;
        this.itemNodes = itemNodes;
        this.wallNodes = wallNodes;
        this.setItems = setItems;
        this.SPACER_COLOR = 0xdc3545;

        this.furnitureItems = [];
        this.selectedItem = null;
        this.isDragging = false;
        this.lastValidPosition = {x: 0, y: 0};

        this.canvas = new DraggableCanvas(this.app);
        this.app.stage.addChild(this.canvas);

        this.initUIElements();

        this.setupEventListeners();

        this.drawRoom();

        this.setupFurnitureList();

        this.drawObjects();
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
    }

    drawRoom() {
        const points = this.wallNodes.flatMap(wallNode => {
            return [wallNode.x, wallNode.y];
        });

        this.room = new PIXI.Graphics();
        this.room.poly(points);
        this.room.fill({color: 0xB7C8F5});
        this.canvas.addChild(this.room);

        this.furnitureContainer = new PIXI.Container();
        this.canvas.addChild(this.furnitureContainer);
    }

    drawObjects() {
        this.itemNodes.forEach(item => {
            const itemInfo = this.setItems.find(x => x.id === item.id);

            this.addFurnitureItem({
                ...item,
                name: itemInfo.name,
                height: itemInfo.height,
                width: itemInfo.width,
                color: this.stringToColor(itemInfo.name)
            })
        });

        this.spacerNodes.forEach(item => {
            this.addFurnitureItem({
                ...item,
                name: "Spacer",
                color: this.SPACER_COLOR
            })
        });
    }

    stringToColor(str) {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }

        let color = Math.abs(hash).toString(16);
        color = color.substring(0, 6);
        while (color.length < 6) {
            color = '0' + color;
        }

        return parseInt(color, 16);
    }

    setupEventListeners() {
        // rotation slider events
        this.rotationSlider.addEventListener('input', () => {
            if (this.selectedItem) {
                const rotation = parseInt(this.rotationSlider.value);
                this.rotateItem(this.selectedItem, rotation);
                this.rotationValue.textContent = `${rotation}°`;
            }
        });

        // spacer button
        document.getElementById('add-spacer').addEventListener('click', () => {
            this.showSpacerDialog();
        });

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

                this.addFurnitureItem({
                    x: centerPosition.x,
                    y: centerPosition.y,
                    id: id,
                    name: name,
                    width: width,
                    height: height,
                    color: this.stringToColor(name)
                });
            });
        });
    }

    addFurnitureItem(itemData) {
        const furniture = new PIXI.Graphics();
        furniture.id = itemData.id || -1;
        furniture.label = itemData.name;
        furniture.rect(0, 0, itemData.width, itemData.height);
        furniture.fill(itemData.color);

        furniture.pivot.set(itemData.width / 2, itemData.height / 2);
        furniture.position.set(itemData.x, itemData.y);
        furniture.rotation = itemData.angle || 0;

        furniture.originalWidth = itemData.width;
        furniture.originalHeight = itemData.height;

        furniture.eventMode = 'static';
        furniture.cursor = 'pointer';

        // Add text label
        const label = new PIXI.Text({
            text: furniture.name, style: {
                fontFamily: 'Arial',
                fontSize: 8,
                fill: 0x000000,
                align: 'center',
            }
        });

        label.anchor.set(0.5);
        label.position.set(itemData.width / 2, itemData.height / 2);
        furniture.addChild(label);

        this.setupDraggableItem(furniture);

        if (!this.isInsideRoom(furniture)) {
            alert(`${itemData.name} doesn't fit in the room.`);
            return;
        }

        this.furnitureContainer.addChild(furniture);
        this.furnitureItems.push(furniture);

        this.selectItem(furniture);
    }

    setupDraggableItem(item) {
        item
            .on('pointerdown', this.onDragStart.bind(this, item))
            .on('pointerup', this.onDragEnd.bind(this, item))
            .on('pointerupoutside', this.onDragEnd.bind(this, item))
            .on('pointermove', this.onDragMove.bind(this, item))
            .on('click', () => this.selectItem(item));
    }

    onDragStart(item, event) {
        if (!this.selectedItem || this.selectedItem !== item) {
            this.selectItem(item);
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

            if (this.checkCollision(item) || !this.isInsideRoom(item)) {
                item.position.set(oldX, oldY);
            } else {
                this.lastValidPosition = {x: item.x, y: item.y};
            }

            this.updatePositionInfo(item);
        }
    }

    selectItem(item) {
        if (this.selectedItem) {
            this.selectedItem.tint = 0xFFFFFF;
        }

        this.selectedItem = item;
        this.selectedItem.tint = 0xFFCC00;

        // update ui
        this.rotationPanel.style.display = 'block';
        this.rotationSlider.value = (item.rotation * 180 / Math.PI) % 360;
        this.rotationValue.textContent = `${Math.round((item.rotation * 180 / Math.PI) % 360)}°`;
        this.selectedItemText.textContent = `Selected: ${item.name}`;
        this.updatePositionInfo(item);
    }

    updatePositionInfo(item) {
        this.positionInfo.textContent = `Position: X: ${Math.round(item.x)} Y: ${Math.round(item.y)}`;
    }

    rotateItem(item, degrees) {
        item.rotation = MathUtils.degreesToRadians(degrees);

        if (!this.isInsideRoom(item) || this.checkCollision(item)) {
            this.nudgeItemIntoRoom(item);
        }
    }

    nudgeItemIntoRoom(item) {
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
    checkCollision(item) {
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

    showSpacerDialog() {
        const width = parseInt(prompt("Enter spacer width (cm):", "50"));
        const height = parseInt(prompt("Enter spacer height (cm):", "50"));

        if (isNaN(width) || isNaN(height) || width <= 0 || height <= 0) {
            alert("Please enter valid positive dimensions for the spacer.");
            return;
        }

        const centerPosition = this.calculateRoomCenter();

        this.addFurnitureItem({
            x: centerPosition.x,
            y: centerPosition.y,
            name: "Spacer",
            width: width,
            height: height,
            color: this.SPACER_COLOR
        });
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
