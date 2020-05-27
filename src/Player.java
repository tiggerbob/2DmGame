public class Player implements GameObject{
    Rectangle playerRectangle;
    int speed = 10;

    //0 = Right, 1 = Left, 2 = Up, 3 = Down
    private int direction = 0;
    private Sprite sprite;
    private AnimatedSprite animatedSprite = null;

    public Player(Sprite sprite) {
        this.sprite = sprite;

        if (sprite != null && sprite instanceof AnimatedSprite)
            animatedSprite = (AnimatedSprite) sprite;

        updateDirection();
        playerRectangle = new Rectangle(32, 16, 16, 32);
        playerRectangle.generateGraphics(3, 0xFF00FF90);
    }

    private void updateDirection() {
        if (animatedSprite != null){
            animatedSprite.setAnimationRange(direction * 8, (direction * 8) + 7);
        }
    }

    @Override
    public void render(RenderHandler renderer, int xZoom, int yZoom) {
        if (animatedSprite != null)
            renderer.renderSprite(animatedSprite, playerRectangle.x, playerRectangle.y, xZoom, yZoom, false);
        else if (sprite != null)
            renderer.renderSprite(sprite, playerRectangle.x, playerRectangle.y, xZoom, yZoom, false);
        else
            renderer.renderSprite(sprite, playerRectangle.x, playerRectangle.y, xZoom, yZoom, false);
    }

    @Override
    public void update(Game game) {
        KeyBoardListener keyListener = game.getKeyListener();

        boolean didMove = false;
        int newDirection = direction ;

        if(keyListener.left()) {
            playerRectangle.x -= speed;
            didMove = true;
            newDirection = 1;
        }
        if(keyListener.right()) {
            playerRectangle.x += speed;
            didMove = true;
            newDirection = 0;
        }
        if(keyListener.up()) {
            playerRectangle.y -= speed;
            didMove = true;
            newDirection = 2;
        }
        if (keyListener.down()) {
            playerRectangle.y += speed;
            didMove = true;
            newDirection = 3;
        }
        if (newDirection != direction) {
            direction = newDirection;
            updateDirection();
        }

        if (!didMove) {
            animatedSprite.reset();
        }

        updateCamera(game.getRenderer().getCamera());

        if (didMove)
            animatedSprite.update(game);
    }

    public void updateCamera(Rectangle camera) {
        camera.x = playerRectangle.x - (camera.w / 2);
        camera.y = playerRectangle.y - (camera.h / 2);
    }

    public boolean handleMouseClick(Rectangle mouseRectangle, Rectangle camera, int xZoom, int yZoom) {return false;}
}
