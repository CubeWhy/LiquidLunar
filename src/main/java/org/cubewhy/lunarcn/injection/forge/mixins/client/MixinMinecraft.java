package org.cubewhy.lunarcn.injection.forge.mixins.client;


import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.*;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.cubewhy.lunarcn.Client;
import org.cubewhy.lunarcn.event.events.*;
import org.cubewhy.lunarcn.files.configs.ClientConfigFile;
import org.cubewhy.lunarcn.gui.SplashProgress;
import org.cubewhy.lunarcn.utils.FileUtils;
import org.cubewhy.lunarcn.utils.GitUtils;
import org.cubewhy.lunarcn.utils.ImageUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.cubewhy.lunarcn.utils.ClientUtils.logger;

@Mixin(Minecraft.class)
abstract public class MixinMinecraft {
    @Shadow
    public GuiScreen currentScreen;

    @Shadow
    public boolean skipRenderWorld;

    @Shadow
    public int leftClickCounter;

    @Shadow
    public MovingObjectPosition objectMouseOver;

    @Shadow
    public WorldClient theWorld;

    @Shadow
    public EntityPlayerSP thePlayer;

    @Shadow
    public EffectRenderer effectRenderer;

    @Shadow
    public PlayerControllerMP playerController;

    @Shadow
    public int rightClickDelayTimer;

    @Shadow
    public GameSettings gameSettings;

    @Shadow
    @Final
    public File mcDataDir;

    @Shadow
    public int displayWidth;

    @Shadow
    public int displayHeight;
    @Shadow
    private boolean fullscreen;
    @Shadow
    public GuiIngame ingameGUI;
    @Shadow
    private SoundHandler mcSoundHandler;

    @Shadow
    public abstract void setIngameNotInFocus();

    @Shadow
    public abstract void setIngameFocus();

    @Inject(method = "startGame", at = @At("RETURN"))
    public void startGameReturn(CallbackInfo ci) throws IOException {
        Client.getInstance().onStart();
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 1, shift = At.Shift.AFTER))
    public void step1(CallbackInfo ci) {
        SplashProgress.setProgress(2, "textures");
    }

    @Inject(method = "startGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;checkGLError(Ljava/lang/String;)V", ordinal = 2, shift = At.Shift.AFTER))
    public void step2(CallbackInfo ci) {
        SplashProgress.setProgress(3, "Gui");
    }

    @Inject(method = "shutdown", at = @At("HEAD"))
    public void shutdown(CallbackInfo ci) {
        Client.getInstance().onStop();
    }

    @Inject(method = "runTick", at = @At("RETURN"))
    public void runTick(CallbackInfo ci) {
        new TickEvent().call();
    }

    /**
     * @author CubeWhy
     * @reason window icon
     */
    @Overwrite
    private void setWindowIcon() {
        try {
            BufferedImage image = ImageIO.read(FileUtils.getFile(Client.clientLogo));
            ByteBuffer bytebuffer = ImageUtils.readImageToBuffer(ImageUtils.resizeImage(image, 16, 16));
            Display.setIcon(new ByteBuffer[]{
                    bytebuffer,
                    ImageUtils.readImageToBuffer(image)
            });
        } catch (IOException e) {
            logger.catching(e);
        }
    }

    @Inject(method = "displayCrashReport", at = @At(value = "INVOKE", target = "Lnet/minecraft/crash/CrashReport;getFile()Ljava/io/File;"))
    public void displayCrashReport(CrashReport crashReportIn, CallbackInfo ci) {
        String message = crashReportIn.getCauseStackTraceOrString();
        JOptionPane.showMessageDialog(null, "Game crashed!\n" +
                        "Please create a issue: \n" + GitUtils.gitInfo.get("git.remote.origin.url").toString().split("\\.git")[0] + "/issues/new\n" +
                        "Please make a screenshot of this screen and send it to developers\n"
                        + message,
                "oops, game crashed!", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * @author CubeWhy
     * @reason MainMenu redirect
     */
    @Overwrite
    public void displayGuiScreen(GuiScreen guiScreenIn) {
        if (guiScreenIn == null && this.theWorld == null || guiScreenIn instanceof GuiMainMenu) {
            guiScreenIn = ClientConfigFile.getInstance().getMenuStyle();
        } else if (guiScreenIn == null && this.thePlayer.getHealth() <= 0.0F) {
            guiScreenIn = new GuiGameOver();
        }

        GuiScreen old = this.currentScreen;
        GuiOpenEvent event = new GuiOpenEvent(guiScreenIn);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            guiScreenIn = event.gui;
            if (old != null && guiScreenIn != old) {
                old.onGuiClosed();
            }

            if (guiScreenIn instanceof GuiMainMenu) {
                this.gameSettings.showDebugInfo = false;
                this.ingameGUI.getChatGUI().clearChatMessages();
            }

            this.currentScreen = guiScreenIn;
            if (guiScreenIn != null) {
                this.setIngameNotInFocus();
                ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
                int i = scaledresolution.getScaledWidth();
                int j = scaledresolution.getScaledHeight();
                guiScreenIn.setWorldAndResolution(Minecraft.getMinecraft(), i, j);
                this.skipRenderWorld = false;
            } else {
                this.mcSoundHandler.resumeSounds();
                this.setIngameFocus();
            }
        }
        new ScreenChangeEvent(guiScreenIn).call();
    }

    @Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("RETURN"))
    public void loadWorld(WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
        new WorldEvent(worldClientIn).call(); // call worldEvent
    }

    @Inject(method = "clickMouse", at = @At("HEAD"))
    public void clickMouse(CallbackInfo ci) {
        new MouseEvent(MouseEvent.MouseButton.LEFT).call();
    }
    @Inject(method = "rightClickMouse", at = @At("HEAD"))
    public void rightClickMouse(CallbackInfo ci) {
        new MouseEvent(MouseEvent.MouseButton.RIGHT).call();
    }

    @Inject(method = "middleClickMouse", at = @At("HEAD"))
    public void middleClickMouse(CallbackInfo ci) {
        new MouseEvent(MouseEvent.MouseButton.MIDDLE).call();
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;setKeyBindState(IZ)V", ordinal = 1), cancellable = true)
    public void keyPressed(CallbackInfo ci) {
        if (new KeyEvent(Keyboard.getEventKey()).call().isCanceled()) {
            ci.cancel(); // cancel event
        }
    }
}
