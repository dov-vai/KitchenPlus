class RoomEditor {
    constructor(app, containerElementId, setId) {
        this.app = app;
        this.setId = setId;
        this.containerElement = document.getElementById(containerElementId);

        this.NODE_RADIUS = 8;
        this.LINE_THICKNESS = 3;
        this.HIT_AREA_PADDING = 10;

        this.nodes = [];
        this.nodeIdCounter = 0;
        this.selectedNode = null;
        this.selectedEdge = null;

        this.initUIElements();

        this.canvas = new DraggableCanvas(app);
        this.app.stage.addChild(this.canvas);

        this.setupEventListeners();

        this.initializeRoomOutline();
    }

    initUIElements() {
        this.addNodeBtn = document.getElementById("addNodeBtn");
        this.saveContourBtn = document.getElementById("saveContourBtn");
        this.nodeControlsUI = document.getElementById("nodeControls");
        this.selectedNodeIdUI = document.getElementById("selectedNodeId");
        this.nodeAngleInput = document.getElementById("nodeAngle");
        this.setNodeAngleBtn = document.getElementById("setNodeAngleBtn");
        this.removeNodeBtn = document.getElementById("removeNodeBtn");
        this.edgeControlsUI = document.getElementById("edgeControls");
        this.selectedEdgeNodesUI = document.getElementById("selectedEdgeNodes");
        this.edgeDistanceInput = document.getElementById("edgeDistance");
        this.setEdgeDistanceBtn = document.getElementById("setEdgeDistanceBtn");
    }

    setupEventListeners() {
        this.addNodeBtn.addEventListener("click", this.addNode.bind(this));
        this.removeNodeBtn.addEventListener("click", this.deleteNode.bind(this));
        this.setEdgeDistanceBtn.addEventListener("click", this.updatePosByDistance.bind(this));
        this.setNodeAngleBtn.addEventListener("click", this.updatePosByAngle.bind(this));
        this.saveContourBtn.addEventListener("click", this.savePlan.bind(this));

        // background click to deselect
        this.canvas.on("pointerdown", (event) => {
            if (event.data.button !== 2) {
                this.deselectAll();
            }
        });
    }

    async savePlan() {
        if (this.nodes.length < 3) {
            alert("Please create at least 3 nodes to form a valid room contour.");
            return;
        }

        const nodeData = this.nodes.map(node => ({
            id: node.id,
            x: node.x,
            y: node.y
        }));

        try {
            const response = await fetch(`/plans/new/${this.setId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify({
                    nodes: nodeData
                })
            });

            if (response.ok) {
                const data = await response.json();
                window.location.href = data.redirect;
            }
        } catch (error) {
            console.error("Error saving contour:", error);
            alert("An error occurred while saving the room contour.");
        }
    }

    deselectAll() {
        this.selectedNode = null;
        this.selectedEdge = null;
        this.redraw();
    }

    selectNode(node) {
        this.deselectAll();
        this.selectedNode = node;
        this.showAngleInputForm();
        this.redraw();
    }

    selectEdge(node1, node2, graphics) {
        this.deselectAll();
        this.selectedEdge = {node1, node2, graphics};
        this.showDistanceInputForm();
        this.redraw();
    }

    drawScene() {
        this.canvas.removeChildren();

        if (this.nodes.length === 0) return;

        this.drawEdges();
        this.drawNodes();
    }

    drawEdges() {
        for (let i = 0; i < this.nodes.length; i++) {
            const node1 = this.nodes[i];
            const node2 = this.nodes[(i + 1) % this.nodes.length]; // connect last to first

            // draw visible edge
            const line = new PIXI.Graphics();
            line.moveTo(node1.x, node1.y);
            line.lineTo(node2.x, node2.y);
            line.stroke({
                width: this.LINE_THICKNESS,
                color: (this.selectedEdge &&
                    this.selectedEdge.node1.id === node1.id &&
                    this.selectedEdge.node2.id === node2.id) ? 0xFF0000 : 0x007bff
            });
            this.canvas.addChild(line);

            this.drawEdgeDistance(node1, node2);

            this.createEdgeHitArea(node1, node2, line);
        }
    }

    drawEdgeDistance(node1, node2) {
        const distance = MathUtils.getDistance(node1, node2);
        const formattedDistance = distance.toFixed(0) + " cm";

        const midX = (node1.x + node2.x) / 2;
        const midY = (node1.y + node2.y) / 2;

        const distanceText = new PIXI.Text({
            text: formattedDistance,
            style: {
                fontFamily: "Arial",
                fontSize: 12,
                fill: 0x000000,
                stroke: {color: 0xFFFFFF, width: 3},
                align: "center"
            }
        });
        distanceText.anchor.set(0.5);

        // position text perpendicular to the edge
        const angle = MathUtils.getAngle(node1, node2);
        const offsetDistance = 15;
        distanceText.x = midX - Math.sin(angle) * offsetDistance;
        distanceText.y = midY + Math.cos(angle) * offsetDistance;

        this.canvas.addChild(distanceText);
    }

    createEdgeHitArea(node1, node2, visibleLine) {
        const hitAreaLine = new PIXI.Graphics();
        hitAreaLine.moveTo(node1.x, node1.y);
        hitAreaLine.lineTo(node2.x, node2.y);
        hitAreaLine.stroke({
            width: this.LINE_THICKNESS + this.HIT_AREA_PADDING,
            color: 0x000000,
            alpha: 0
        });
        hitAreaLine.eventMode = "static";
        hitAreaLine.on("pointerdown", (event) => {
            event.stopPropagation();
            this.selectEdge(node1, node2, visibleLine);
        });

        this.canvas.addChild(hitAreaLine);
    }

    drawNodes() {
        this.nodes.forEach(node => {
            // draw node circle
            const nodeGraphic = new PIXI.Graphics();
            nodeGraphic.circle(0, 0, this.NODE_RADIUS);
            nodeGraphic.stroke({
                width: 2,
                color: (this.selectedNode && this.selectedNode.id === node.id) ? 0xFF0000 : 0x000000
            });
            nodeGraphic.fill({
                color: 0xFFFFFF
            });
            nodeGraphic.x = node.x;
            nodeGraphic.y = node.y;
            nodeGraphic.eventMode = "static";

            nodeGraphic.on("pointerdown", (event) => {
                event.stopPropagation();
                this.selectNode(node);
            });

            node.graphics = nodeGraphic;
            this.canvas.addChild(nodeGraphic);

            // draw node ID
            const nodeIdText = new PIXI.Text({
                text: node.id.toString(),
                style: {
                    fontFamily: "Arial",
                    fontSize: 10,
                    fill: 0x000000,
                    align: "center"
                }
            });
            nodeIdText.anchor.set(0.5);
            nodeIdText.x = node.x;
            nodeIdText.y = node.y;
            nodeIdText.eventMode = "none";
            this.canvas.addChild(nodeIdText);
        });
    }

    redraw() {
        this.updateUI();
        this.drawScene();
    }

    showDistanceInputForm(){
        if (this.selectedEdge) {
            this.edgeControlsUI.classList.remove("hidden");
            this.selectedEdgeNodesUI.textContent = `Node ${this.selectedEdge.node1.id} to Node ${this.selectedEdge.node2.id}`;
            const distance = MathUtils.getDistance(this.selectedEdge.node1, this.selectedEdge.node2);
            this.edgeDistanceInput.value = distance.toFixed(2);
        } else {
            this.edgeControlsUI.classList.add("hidden");
        }
    }

    showAngleInputForm(){
        if (this.selectedNode) {
            this.nodeControlsUI.classList.remove("hidden");
            this.selectedNodeIdUI.textContent = this.selectedNode.id;

            const nodeIndex = this.nodes.findIndex(n => n.id === this.selectedNode.id);
            if (nodeIndex !== -1 && this.nodes.length > 1) {
                const nextNode = this.nodes[(nodeIndex + 1) % this.nodes.length];
                const currentAngleRad = MathUtils.getAngle(this.selectedNode, nextNode);
                this.nodeAngleInput.value = MathUtils.radiansToDegrees(currentAngleRad).toFixed(2);
            } else {
                this.nodeAngleInput.value = "";
            }
        } else {
            this.nodeControlsUI.classList.add("hidden");
        }
    }

    updateUI() {
        this.showAngleInputForm();
        this.showDistanceInputForm();
    }

    addNode() {
        const newNodeId = this.nodeIdCounter++;
        let x, y;

        if (this.nodes.length === 0) {
            // center of current view
            const localCenter = this.canvas.toLocal(
                new PIXI.Point(this.app.screen.width / 2, this.app.screen.height / 2)
            );
            x = localCenter.x;
            y = localCenter.y;
        } else if (this.nodes.length === 1) {
            x = this.nodes[0].x + 100; // 100cm away
            y = this.nodes[0].y;
        } else {
            const lastNode = this.nodes[this.nodes.length - 1];
            const secondLastNode = this.nodes[this.nodes.length - 2];
            // offset the same amount as second to last node
            const dx = lastNode.x - secondLastNode.x;
            const dy = lastNode.y - secondLastNode.y;
            const dist = Math.sqrt(dx * dx + dy * dy) || 100; // default if coincident
            x = lastNode.x + (dx / dist) * 100;
            y = lastNode.y + (dy / dist) * 100;
        }

        const newNode = new Node(newNodeId, x, y);
        this.nodes.push(newNode);
        this.selectNode(newNode);
    }

    deleteNode() {
        if (!this.selectedNode || this.nodes.length <= 3) {
            alert("Cannot remove node. A polygon needs at least 3 nodes.");
            return;
        }
        this.nodes = this.nodes.filter(n => n.id !== this.selectedNode.id);
        this.deselectAll();
    }

    updatePosByDistance() {
        if (!this.selectedEdge) return;

        const newDistance = parseFloat(this.edgeDistanceInput.value);
        if (isNaN(newDistance) || newDistance <= 0) {
            alert("Please enter a valid positive distance.");
            return;
        }

        const {node1, node2} = this.selectedEdge;
        const currentDist = MathUtils.getDistance(node1, node2);

        if (Math.abs(currentDist - newDistance) < 0.01) return; // no change

        const dx = node2.x - node1.x;
        const dy = node2.y - node1.y;

        if (currentDist === 0) {
            // nodes are coincident, move along x-axis
            node2.x = node1.x + newDistance;
            node2.y = node1.y;
        } else {
            const ratio = newDistance / currentDist;
            node2.x = node1.x + dx * ratio;
            node2.y = node1.y + dy * ratio;
        }

        const node2Index = this.nodes.findIndex(n => n.id === node2.id);
        if (node2Index > -1) {
            this.nodes[node2Index].x = node2.x;
            this.nodes[node2Index].y = node2.y;
        }

        this.redraw();
    }

    updatePosByAngle() {
        // need at least 2 nodes for an angle
        if (!this.selectedNode || this.nodes.length < 2) return;

        const newAngleDegrees = parseFloat(this.nodeAngleInput.value);
        if (isNaN(newAngleDegrees)) {
            alert("Please enter a valid angle.");
            return;
        }
        const newAngleRadians = MathUtils.degreesToRadians(newAngleDegrees);

        const pivotNode = this.selectedNode;
        const pivotNodeIndex = this.nodes.findIndex(n => n.id === pivotNode.id);
        if (pivotNodeIndex === -1) return;

        const nextNode = this.nodes[(pivotNodeIndex + 1) % this.nodes.length];
        const distance = MathUtils.getDistance(pivotNode, nextNode);

        if (distance === 0) {
            // if coincident, give a default 100 cm distance
            nextNode.x = pivotNode.x + 100 * Math.cos(newAngleRadians);
            nextNode.y = pivotNode.y + 100 * Math.sin(newAngleRadians);
        } else {
            nextNode.x = pivotNode.x + distance * Math.cos(newAngleRadians);
            nextNode.y = pivotNode.y + distance * Math.sin(newAngleRadians);
        }

        const actualNextNodeIndex = this.nodes.findIndex(n => n.id === nextNode.id);
        if (actualNextNodeIndex > -1) {
            this.nodes[actualNextNodeIndex].x = nextNode.x;
            this.nodes[actualNextNodeIndex].y = nextNode.y;
        }

        this.redraw();
    }

    initializeRoomOutline() {
        this.addNode();
        this.addNode();
        this.addNode();
        this.addNode();
        this.nodes[1].y -= 300;
        this.nodes[1].x -= 100;
        this.nodes[2].y -= 300;
        this.nodes[2].x += 100;
        this.deselectAll();
    }
}