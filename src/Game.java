import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Game extends JFrame implements Runnable{

    public static int alpha = 0xFFFF00DC;

    private Canvas canvas = new Canvas();
    private RenderHandler renderer;

    private SpriteSheet sheet;
    private SpriteSheet playerSheet;

    private int selectedTileID = 2;

    private Tiles tiles;
    private Map map;
    private int zoom = 3;

    private GameObject[] objects;
    private KeyBoardListener keyListener = new KeyBoardListener(this);
    private MouseEventListener mouseListener = new MouseEventListener(this);

    private Player player;

    public Game() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setBounds(0, 0, 1000, 800);

        setLocationRelativeTo(null);

        add(canvas);

        setVisible(true);

        setResizable(false);

        canvas.createBufferStrategy(3);

        renderer = new RenderHandler(getWidth(), getHeight());

        //Load Assets
        BufferedImage sheetImage = loadImage("Tiles1.png");
        sheet = new SpriteSheet(sheetImage);
        sheet.loadSprites(16, 16);

        BufferedImage playerSheetImage = loadImage("Player.png");
        playerSheet = new SpriteSheet(playerSheetImage);
        playerSheet.loadSprites(20, 26);
        AnimatedSprite playerAnimations = new AnimatedSprite(playerSheet, 5);

        //Load Tiles
        tiles = new Tiles(new File("src/Tiles.txt"), sheet);

        //Load Map
        map = new Map(new File("src/Map.txt"), tiles);

        //Load SDK GUI
        GUIButton[] buttons = new GUIButton[tiles.size()];
        Sprite[] tileSprites = tiles.getSprites();

        for (int i = 0; i < buttons.length; i++) {
            Rectangle tileRectangle = new Rectangle(0, i*(16*zoom + 2), 16*zoom, 16*zoom);
            buttons[i] = new SDKButton(this, i, tileSprites[i], tileRectangle);
        }

        GUI gui = new GUI(buttons, 5, 5, true);

        //Load Objects
        objects = new GameObject[2];
        player = new Player(playerAnimations);
        objects[0] = player;
        objects[1] = gui;

        //Add Listeners
        canvas.addKeyListener(keyListener);
        canvas.addFocusListener(keyListener);
        canvas.addMouseListener(mouseListener);
        canvas.addMouseMotionListener(mouseListener);
    }

    public void update() {
        for(int i = 0; i < objects.length; i++)
            objects[i].update(this);
    }

    private BufferedImage loadImage(String path) {
        try {
            BufferedImage loadedImage = ImageIO.read(Game.class.getResource(path));
            BufferedImage formattedImage = new BufferedImage(loadedImage.getWidth(), loadedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            formattedImage.getGraphics().drawImage(loadedImage, 0, 0, null);

            return formattedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void handleCTRL(boolean[] keys) {
        if(keys[KeyEvent.VK_S])
            map.saveMap();
    }

    public void leftClick(int x, int y) {
        Rectangle mouseRectangle = new Rectangle(x, y, 1, 1);
        boolean stoppedChecking = false;

        for (int i = 0; i < objects.length; i++) {
            if (!stoppedChecking)
                stoppedChecking = objects[i].handleMouseClick(mouseRectangle, renderer.getCamera(), zoom, zoom);
        }
        if (!stoppedChecking) {
            x = (int) Math.floor((x + renderer.getCamera().x) / (16.0 * zoom));
            y = (int) Math.floor((y + renderer.getCamera().y) / (16.0 * zoom));
            map.setTile(x, y, selectedTileID);
        }
    }

    public void rightClick(int x, int y) {
        x = (int) Math.floor((x + renderer.getCamera().x) / (16.0 * zoom));
        y = (int) Math.floor((y + renderer.getCamera().y) / (16.0 * zoom));
        map.removeTile(x, y);
    }

    public void render() {
        BufferStrategy bufferStrategy = canvas.getBufferStrategy();
        Graphics graphics = bufferStrategy.getDrawGraphics();
        super.paint(graphics);

        map.render(renderer, zoom, zoom);

        for(int i = 0; i < objects.length; i++)
            objects[i].render(renderer, zoom, zoom);

        renderer.render(graphics);

        graphics.dispose();
        bufferStrategy.show();
        renderer.clear();

    }

    public void changeTile(int tileID) {
        selectedTileID = tileID;
    }

    public int getSelectedTile() {
        return selectedTileID;
    }

    @Override
    public void run() {
        BufferStrategy bufferStrategy = canvas.getBufferStrategy();

        Long lastTime = System.nanoTime();
        double nanoSecondConversion = 1000000000.0 / 60;
        double changeInSeconds = 0;

        while (true) {
            Long now = System.nanoTime();

            changeInSeconds += (now - lastTime) / nanoSecondConversion;

            while (changeInSeconds >= 1) {
                update();
                changeInSeconds = 0;
            }

            render();
            lastTime = now;
        }
    }

    public static void main(String[] args) {
        Game game = new Game();
        Thread gameThread = new Thread(game);
        gameThread.start();
    }

    public KeyBoardListener getKeyListener() {
        return keyListener;
    }

    public MouseEventListener getMouseListener() {
        return mouseListener;
    }

    public RenderHandler getRenderer() {
        return renderer;
    }
}
