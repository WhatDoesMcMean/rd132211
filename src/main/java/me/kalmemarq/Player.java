package me.kalmemarq;

import me.kalmemarq.render.Window;
import me.kalmemarq.util.BlockHitResult;
import me.kalmemarq.util.Box;
import me.kalmemarq.util.Keybinding;
import me.kalmemarq.util.MathUtils;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

public class Player {
    public final World world;
    public Vector3f position = new Vector3f();
    public Vector3f prevPosition = new Vector3f();
    public Vector3f velocity = new Vector3f();
    public Vector2f size = new Vector2f(0.6f, 1.8f);
    public float yaw = 90f;
    public float pitch;
    public float eyeHeight = this.size.y - 0.18f;
    public Box box;
    public boolean onGround;
    public boolean canFly;
    public boolean noClip;

    public Player(World world) {
        this.world = world;
        this.goToRandomPosition();
    }

    public void goToRandomPosition() {
        this.position.x = (float) Math.random() * (float) this.world.width;
        this.position.y = (float) (this.world.depth + 10);
        this.position.z = (float) Math.random() * (float) this.world.height;
        this.prevPosition.set(this.position);
        this.box = new Box(this.position.x - this.size.x / 2, this.position.y, this.position.z - this.size.x / 2, this.position.x + this.size.x / 2, this.position.y + this.size.y, this.position.z + this.size.x / 2);
    }

    public void turn(float dx, float dy) {
        this.yaw = MathUtils.wrapDegrees(this.yaw + dx, -180, 180);
        this.pitch = Math.clamp(this.pitch + dy, -90, 90);
    }

    public Vector3d getPosition() {
        return new Vector3d(this.position);
    }

    public Vector3d getCameraPosition() {
        return this.getPosition().add(0, this.eyeHeight, 0);
    }

    public Vector3d getLook() {
        double cosYaw = Math.cos(Math.toRadians(-this.yaw));
        double sinYaw = Math.sin(Math.toRadians(-this.yaw));
        double cosPitch = -Math.cos(Math.toRadians(-this.pitch));
        double sinPitch = Math.sin(Math.toRadians(-this.pitch));
        return new Vector3d((sinYaw * cosPitch), sinPitch, (cosYaw * cosPitch));
    }

    public BlockHitResult raytrace(double reach) {
        Vector3d start = this.getCameraPosition();
        Vector3d look = this.getLook();
        Vector3d end = start.add(look.x() * reach, look.y() * reach, look.z() * reach, new Vector3d());
        return this.world.raytraceBlock(start.x, start.y, start.z, end.x, end.y, end.z);
    }

    public void tick(Window window) {
        this.prevPosition.set(this.position);
        float xd = 0;
        float zd = 0;

        if (Keybinding.FOWARDS.isPressed(window)) {
            zd -= 1;
        }

        if (Keybinding.BACKWARD.isPressed(window)) {
            zd += 1;
        }

        if (Keybinding.STRAFE_LEFT.isPressed(window)) {
            xd -= 1;
        }

        if (Keybinding.STRAFE_RIGHT.isPressed(window)) {
            xd += 1;
        }

        if ((this.onGround || this.canFly) && Keybinding.JUMP.isPressed(window)) {
            this.velocity.y = 0.12f;
        }

        if (Keybinding.DESCEND.isPressed(window)) {
            this.velocity.y = -0.12f;
        }

        if (!this.canFly) {
            this.velocity.y -= 0.005f;
        }

        float speed = this.onGround ? 0.02f : this.canFly ? 0.02f : 0.005f;

        this.moveRelative(xd, zd, speed);
        this.move(this.velocity.x, this.velocity.y, this.velocity.z);

        this.velocity.mul(0.91f, this.canFly ? 0.91f : 0.98f, 0.91f);

        if (this.onGround) {
            this.velocity.mul(0.8f, 0f, 0.8f);
        }
    }

    public void move(float xd, float yd, float zd) {
        float xdOrg = xd;
        float ydOrg = yd;
        float zdOrg = zd;

        if (!this.canFly || !this.noClip) {
            List<Box> boxes = this.world.getCubes(this.box.grow(xd, yd, zd));

            for (Box box : boxes) {
                yd = box.clipYCollide(this.box, yd);
            }
            this.box.move(0, yd, 0);

            for (Box box : boxes) {
                xd = box.clipXCollide(this.box, xd);
            }
            this.box.move(xd, 0, 0);

            for (Box box : boxes) {
                zd = box.clipZCollide(this.box, zd);
            }
            this.box.move(0, 0, zd);
        } else {
            this.box.move(0, yd, 0);
            this.box.move(xd, 0, 0);
            this.box.move(0, 0, zd);
        }

        if (ydOrg != yd) {
            this.velocity.y = 0f;
        }

        if (xdOrg != xd) {
            this.velocity.x = 0f;
        }

        if (zdOrg != zd) {
            this.velocity.z = 0f;
        }

        this.onGround = ydOrg != yd && ydOrg < 0f;

        this.position.x = (this.box.minX + this.box.maxX) / 2f;
        this.position.y = this.box.minY;
        this.position.z = (this.box.minZ + this.box.maxZ) / 2f;
    }

    private void moveRelative(float xd, float zd, float speed) {
        float distSquared = xd * xd + zd * zd;
        if (distSquared >= 0.01f) {
            float scale = speed / (float) Math.sqrt(distSquared);
            xd *= scale;
            zd *= scale;
            float sin = (float) Math.sin(Math.toRadians(this.yaw));
            float cos = (float) Math.cos(Math.toRadians(this.yaw));
            this.velocity.x += xd * cos - zd * sin;
            this.velocity.z += zd * cos + xd * sin;
        }
    }
}
