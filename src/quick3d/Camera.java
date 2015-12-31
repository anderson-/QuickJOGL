package quick3d;

import com.jogamp.opengl.GL2;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Camera implements MouseMotionListener, MouseListener, KeyListener {

    private Point mouseCenter;

    @Override
    public void mouseDragged(MouseEvent me) {
    }

    @Override
    public void mouseMoved(MouseEvent me) {
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        lx = -1;
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }

    @Override
    public void keyPressed(KeyEvent ke) {

        switch (ke.getKeyCode()) {
            case KeyEvent.VK_W:
                moveForward = true;
                break;
            case KeyEvent.VK_S:
                moveBackward = true;
                break;
            case KeyEvent.VK_Q:
                lookLeft = true;
                break;
            case KeyEvent.VK_E:
                lookRight = true;
                break;
            case KeyEvent.VK_A:
                strafeLeft = true;
                break;
            case KeyEvent.VK_D:
                strafeRight = true;
                break;
            case KeyEvent.VK_PAGE_UP:
                flyUp = true;
                break;
            case KeyEvent.VK_PAGE_DOWN:
                flyDown = true;
                break;
            case KeyEvent.VK_SPACE:
                flyUp = true;
                break;
            case KeyEvent.VK_SHIFT:
                flyDown = true;
                break;
        }

//
//        if (Keyboard.isKeyDown(KeyEvent.VK_LCONTROL)) {
//            Mouse.setGrabbed(!Mouse.isGrabbed());
//        }
//
//        if (Keyboard.isKeyDown(KeyEvent.VK_HOME) && !Keyboard.isRepeatEvent()) {
//            saveScreenshot();
//        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        switch (ke.getKeyCode()) {
            case KeyEvent.VK_W:
                moveForward = false;
                break;
            case KeyEvent.VK_S:
                moveBackward = false;
                break;
            case KeyEvent.VK_Q:
                lookLeft = false;
                break;
            case KeyEvent.VK_E:
                lookRight = false;
                break;
            case KeyEvent.VK_A:
                strafeLeft = false;
                break;
            case KeyEvent.VK_D:
                strafeRight = false;
                break;
            case KeyEvent.VK_PAGE_UP:
                flyUp = false;
                break;
            case KeyEvent.VK_PAGE_DOWN:
                flyDown = false;
                break;
            case KeyEvent.VK_SPACE:
                flyUp = false;
                break;
            case KeyEvent.VK_ESCAPE:
                System.out.println("ESC key pressed.");
                System.exit(0);
            case KeyEvent.VK_SHIFT:
                flyDown = false;
                break;
            case KeyEvent.VK_T:
                translate = !translate;
                if (translate) {
                    vector.x = 0;
                    vector.y = -1;
                    vector.z = 5.7f;
                    rotation.x = rotation.y = rotation.z = 0;
                    speed = .15f;
                } else {
                    speed = .3f;
                }
                lookRight = translate;
                strafeLeft = translate;
                break;
        }
    }

    class Vector3f {

        public float x;
        public float y;
        public float z;

        public Vector3f() {

        }

        public Vector3f(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

    }

    //Vector3f vector = new Vector3f(0,-5,-10); //look
    Vector3f vector = new Vector3f(0, -1, 5.7f);
    Vector3f rotation = new Vector3f();
    Vector3f vectorPrevious = new Vector3f();
    boolean moveForward = false, moveBackward = false;
    boolean strafeLeft = false, strafeRight = false;
    boolean flyUp = false, flyDown = false;
    boolean lookLeft = false, lookRight = false;
    static float speed = 0.3f;
    DrawingPanel3D scene3d;
    boolean translate = false;
    int lx = -1, ly;
    private final Robot robot;

    public Camera(DrawingPanel3D scene3d) {
        this.scene3d = scene3d;
//        Mouse.setGrabbed(true);
        Robot r = null;
        try {
            r = new Robot();
        } catch (final AWTException e) {
        }

        this.robot = r;
    }

    public void setMouseCenter(Point center) {
        this.mouseCenter = center;
        robot.mouseMove(mouseCenter.x, mouseCenter.y);
    }

    public void update() {
        updatePrevious();

        int x = MouseInfo.getPointerInfo().getLocation().x;
        int y = MouseInfo.getPointerInfo().getLocation().y;

        float mouseDX = (x - mouseCenter.x) * 0.8f * 0.26f;
        float mouseDY = -(y - mouseCenter.y) * 0.8f * 0.26f;
        if (rotation.y + mouseDX >= 360) {
            rotation.y = rotation.y + mouseDX - 360;
        } else if (rotation.y + mouseDX < 0) {
            rotation.y = 360 - rotation.y + mouseDX;
        } else {
            rotation.y += mouseDX;
        }
        if (rotation.x - mouseDY >= -89 && rotation.x - mouseDY <= 89) {
            rotation.x += -mouseDY;
        } else if (rotation.x - mouseDY < -89) {
            rotation.x = -89;
        } else if (rotation.x - mouseDY > 89) {
            rotation.x = 89;
        }

        robot.mouseMove(mouseCenter.x, mouseCenter.y);

        updateVector();
    }

    public void updateVector() {
        if (moveForward) {
            vector.x -= (float) (Math.sin(-rotation.y * Math.PI / 180) * speed);
            vector.z -= (float) (Math.cos(-rotation.y * Math.PI / 180) * speed);
        }
        if (moveBackward) {
            vector.x += (float) (Math.sin(-rotation.y * Math.PI / 180) * speed);
            vector.z += (float) (Math.cos(-rotation.y * Math.PI / 180) * speed);
        }
        if (strafeLeft) {
            vector.x += (float) (Math.sin((-rotation.y - 90) * Math.PI / 180) * speed);
            vector.z += (float) (Math.cos((-rotation.y - 90) * Math.PI / 180) * speed);
        }
        if (strafeRight) {
            vector.x += (float) (Math.sin((-rotation.y + 90) * Math.PI / 180) * speed);
            vector.z += (float) (Math.cos((-rotation.y + 90) * Math.PI / 180) * speed);
        }
        if (flyUp) {
            vector.y += speed;
        }
        if (flyDown) {
            vector.y -= speed;
        }
        if (lookLeft) {
            rotation.y -= speed * 10;
        }
        if (lookRight) {
            rotation.y += speed * 10;
        }
    }

    public void translatePostion(GL2 gl) {
        //This is the code that changes 3D perspective to the camera's perspective.
        gl.glRotatef(rotation.x, 1, 0, 0);
        gl.glRotatef(rotation.y, 0, 1, 0);
        gl.glRotatef(rotation.z, 0, 0, 1);
        //-vector.y-2.4f means that your y is your feet, and y+2.4 is your head.
        gl.glTranslatef(-vector.x, -vector.y - 2.4f, -vector.z);
    }

    public void updatePrevious() {
        //Update last position (for collisions (later))
        vectorPrevious.x = vector.x;
        vectorPrevious.y = vector.y;
        vectorPrevious.z = vector.z;
    }

}
