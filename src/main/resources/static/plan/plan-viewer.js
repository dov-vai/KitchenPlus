class PlanViewer {
    constructor(app, wallNodes, spacerNodes, itemNodes, setItems){
        this.app = app;
        this.wallNodes = wallNodes;
        this.spacerNodes = spacerNodes;
        this.itemNodes = itemNodes;
        this.setItems = setItems;
        this.SPACER_COLOR = 0xdc3545;

        this.canvas = new DraggableCanvas(this.app);
        this.app.stage.addChild(this.canvas);

        this.drawRoom();
        this.drawObjects();
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

    createFurnitureItem(itemData){
        const furniture = new PIXI.Graphics();
        furniture.rect(0, 0, itemData.width, itemData.height);
        furniture.fill(itemData.color);

        furniture.pivot.set(itemData.width / 2, itemData.height / 2);
        furniture.position.set(itemData.x, itemData.y);
        furniture.rotation = itemData.angle || 0;

        // Add text label
        const label = new PIXI.Text({
            text: itemData.name, style: {
                fontFamily: 'Arial',
                fontSize: 8,
                fill: 0x000000,
                align: 'center',
            }
        });

        label.anchor.set(0.5);
        label.position.set(itemData.width / 2, itemData.height / 2);
        furniture.addChild(label);

        return furniture;
    }

    addFurnitureItem(itemData) {
        const furniture = this.createFurnitureItem(itemData);

        this.furnitureContainer.addChild(furniture);
    }
}