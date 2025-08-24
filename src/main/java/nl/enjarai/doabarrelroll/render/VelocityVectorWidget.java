package nl.enjarai.doabarrelroll.render;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.SourceFactor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Smoother;
import net.minecraft.util.math.Vec3d;
import nl.enjarai.doabarrelroll.DoABarrelRoll;
import org.joml.*;

import java.lang.Math;

public class VelocityVectorWidget extends RenderHelper{
    private static final int VELOCITY_VECTOR_WIDTH = 26;
    private static final int VELOCITY_VECTOR_HEIGHT = 26;
    private static final Identifier VELOCITY_VECTOR_TEXTURE = DoABarrelRoll.id("textures/gui/velocity_vector.png");

    private static double target_x = 0;
    private static double target_y = 0;

    private static double current_x = 0;
    private static double current_y = 0;

    private static final RenderPipeline GUI_TEXTURE_INVERTED = RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
            .withLocation("pipeline/crosshair")
            .withBlend(new BlendFunction(SourceFactor.ONE_MINUS_DST_COLOR, DestFactor.ONE_MINUS_SRC_COLOR, SourceFactor.ONE, DestFactor.ZERO))
            .build();

    public static void render(DrawContext context, int scaledWidth, int scaledHeight) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        Camera camera = MinecraftClient.getInstance().gameRenderer.getCamera();
        Camera.Projection projection = camera.getProjection();


        assert player != null;
        Vec3d velocity = player.getVelocity();
        Vec3d direction = player.getRotationVector();
        Quaternionf rotation = camera.getRotation();

        int centerX = scaledWidth / 2 - 1;
        int centerY = scaledHeight / 2 - 1;

        Vector2d velocity_vector_position = calculate_velocity_vector_position(context, velocity,rotation, scaledWidth, scaledHeight, projection);

        velocity_vector_position = new Vector2d(centerX + velocity_vector_position.x, centerY + velocity_vector_position.y);

        draw_velocity_vector(context, (int)velocity_vector_position.x, (int) velocity_vector_position.y) ;
    }

    private static void draw_velocity_vector(DrawContext context, int x, int y) {
        x -= VELOCITY_VECTOR_HEIGHT / 2;
        y -= VELOCITY_VECTOR_WIDTH / 2;

        context.drawTexture(GUI_TEXTURE_INVERTED, VELOCITY_VECTOR_TEXTURE, x, y, 0,0, VELOCITY_VECTOR_WIDTH, VELOCITY_VECTOR_HEIGHT, VELOCITY_VECTOR_WIDTH, VELOCITY_VECTOR_HEIGHT);
    }

    private static Vector2d calculate_velocity_vector_position(DrawContext ctx, Vec3d velocity, Quaternionf rotation, int scaledWidth, int scaledHeight, Camera.Projection projection) {
        Vector3d relative_velocity = new Vector3d();

        rotation.transformInverse(velocity.toVector3d(), relative_velocity);

        double vertical_fov = MinecraftClient.getInstance().options.getFov().getValue() * Math.PI / 180;
        double horizontal_fov = calculate_horizontal_fov(vertical_fov, scaledWidth, scaledHeight);

        double yaw = -Math.atan(relative_velocity.x/relative_velocity.z);
        double pitch = Math.atan(relative_velocity.y/relative_velocity.z);

        double x = (((yaw / (Math.PI)) / (horizontal_fov / Math.PI)) * scaledWidth);
        double y = (((pitch / (Math.PI)) / (vertical_fov / Math.PI)) * scaledHeight);

        target_x = x;
        target_y = y;

        VelocityVectorWidget.current_x = MathHelper.lerp(0.8, current_x, target_x);
        VelocityVectorWidget.current_y = MathHelper.lerp(0.8, current_y, target_y);

        return new Vector2d(VelocityVectorWidget.current_x, VelocityVectorWidget.current_y);
    }

    private static double calculate_horizontal_fov(double verticalFov, int scaledWidth, int scaledHeight) {
        return 2 * Math.atan(Math.tan(verticalFov / 2) * ((double) scaledWidth / (double) scaledHeight));
    }

    public static void clearValues() {
        current_x = target_x;
        current_y = target_y;
    }
}