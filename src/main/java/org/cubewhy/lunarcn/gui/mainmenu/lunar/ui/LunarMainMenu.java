package org.cubewhy.lunarcn.gui.mainmenu.lunar.ui;

import net.minecraftforge.client.gui.ForgeGuiFactory;
import net.minecraftforge.fml.client.GuiModList;
import org.cubewhy.lunarcn.gui.mainmenu.lunar.font.FontUtil;
import org.cubewhy.lunarcn.gui.mainmenu.lunar.ui.buttons.AccountButton;
import org.cubewhy.lunarcn.gui.mainmenu.lunar.ui.buttons.QuitButton;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.cubewhy.lunarcn.Client;
import org.cubewhy.lunarcn.account.IAccount;
import org.cubewhy.lunarcn.event.EventTarget;
import org.cubewhy.lunarcn.event.events.SessionEvent;
import org.cubewhy.lunarcn.files.configs.AccountConfigFile;
import org.cubewhy.lunarcn.gui.elements.ImageButton;
import org.cubewhy.lunarcn.gui.elements.LunarButton;
import org.cubewhy.lunarcn.gui.hud.HudManager;
import org.cubewhy.lunarcn.utils.GitUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.awt.*;
import java.util.*;
import java.util.List;

public class LunarMainMenu extends GuiMainMenu {

    private ResourceLocation logo;

    private LunarButton btnSinglePlayer;
    private LunarButton btnMultiplayer;

    private ImageButton btnClientOptions;
    private ImageButton btnCosmetics;
    private ImageButton btnMinecraftOptions;
    private ImageButton btnLanguage;
    private ImageButton btnForgeModList;

    private QuitButton btnQuit;

    private static final ResourceLocation[] titlePanoramaPaths = new ResourceLocation[]{
            new ResourceLocation("lunar/panorama/panorama_0.png"),
            new ResourceLocation("lunar/panorama/panorama_1.png"),
            new ResourceLocation("lunar/panorama/panorama_2.png"),
            new ResourceLocation("lunar/panorama/panorama_3.png"),
            new ResourceLocation("lunar/panorama/panorama_4.png"),
            new ResourceLocation("lunar/panorama/panorama_5.png")
    };

    private static int panoramaTimer;
    private ResourceLocation backgroundTexture;
    private DropDownList altList;

    @Override
    public void initGui() {
//        this.backgroundTexture = this.mc.getTextureManager().getDynamicTextureLocation("background", new DynamicTexture(256, 256));

        this.logo = new ResourceLocation("lunarcn/logo.png");

        this.btnSinglePlayer = new LunarButton("S I N G L E P L A Y E R", this.width / 2 - 66, this.height / 2);
        this.btnMultiplayer = new LunarButton("M U L T I P L A Y E R", this.width / 2 - 66, this.height / 2 + 15);
        this.altList = new DropDownList(10, 10); // TODO 实现altmanager

        int yPos = this.height - 20;
        this.btnClientOptions = new ImageButton("SETTINGS", new ResourceLocation("lunar/icons/lunar.png"), this.width / 2 - 30, yPos);
        this.btnCosmetics = new ImageButton("COSMETICS", new ResourceLocation("lunar/icons/cosmetics.png"), this.width / 2 - 15, yPos);
        this.btnMinecraftOptions = new ImageButton("MINECRAFT SETTINGS", new ResourceLocation("lunar/icons/cog.png"), this.width / 2, yPos);
        this.btnLanguage = new ImageButton("LANGUAGE", new ResourceLocation("lunar/icons/globe.png"), this.width / 2 + 15, yPos);
        this.btnForgeModList = new ImageButton("FORGE MODS", new ResourceLocation("lunarcn/icons/forge.png"), this.width / 2 + 30, yPos);

        this.btnQuit = new QuitButton(this.width - 17, 7);

        this.updateAccounts();
    }

    @EventTarget
    public void onSession(SessionEvent event) {
        this.updateAccounts();
    }

    public void updateAccounts() {
        IAccount[] accounts = AccountConfigFile.getInstance().getAccounts();
        List<AccountButton> items = new ArrayList<>(Collections.emptyList());
        for (IAccount account :
                accounts) {
            AccountButton accountButton = new AccountButton(account);
            items.add(accountButton);
        }
        altList.items = items.toArray(new AccountButton[0]);

        ArrayList<AccountButton> items1 = new ArrayList<>(Arrays.asList(altList.items));
        if (!items1.isEmpty() && !Objects.equals(items1.get(0).account.getUserName(), altList.currentItem.account.getUserName())) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).account.getUserName().equals(altList.currentItem.account.getUserName())) {
                    altList.currentItem = items.get(i);
                    items.remove(i); // TODO 最终不这样实现
                    items1.add(0, altList.currentItem);
                }
            }
            altList.items = items1.toArray(new AccountButton[]{});
        }

        altList.reset();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (this.btnSinglePlayer.hoverFade > 0) {
            mc.displayGuiScreen(new GuiSelectWorld(this));
        }
        if (this.btnMultiplayer.hoverFade > 0) {
            mc.displayGuiScreen(new GuiMultiplayer(this));
        }
        if (this.btnQuit.hoverFade > 0) {
            mc.shutdown();
        }
        if (this.btnMinecraftOptions.hoverFade > 0) {
            mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings));
        }
        if (this.btnLanguage.hoverFade > 0) {
            mc.displayGuiScreen(new GuiLanguage(this, this.mc.gameSettings, this.mc.getLanguageManager()));
        }
        if (this.btnClientOptions.hoverFade > 0) {
            HudManager.getInstance().openConfigScreen();
        }
        if (this.btnForgeModList.hoverFade > 0) {
            mc.displayGuiScreen(new GuiModList(mc.currentScreen));
        }
        if (this.altList.hoverFade > 0) {
            AccountButton accountButton = this.altList.getCurrentHeld();
            this.altList.currentItem = accountButton;
            updateAccounts(); // 更新账户列表
            accountButton.account.switchAccount(); // 切换账户
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableAlpha();
        this.renderSkybox(mouseX, mouseY, partialTicks);
        GlStateManager.enableAlpha();

        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(logo);
        Gui.drawModalRectWithCustomSizedTexture(this.width / 2 - 25, this.height / 2 - 68, 0, 0, 49, 49, 49, 49);

        FontUtil.TITLE.getFont().drawCenteredString(Client.clientName, (float) this.width / 2 - 0.25F, this.height / 2 - 18, new Color(30, 30, 30, 70).getRGB());
        FontUtil.TITLE.getFont().drawCenteredString(Client.clientName, (float) this.width / 2, (float) this.height / 2 - 19, -1);

        this.btnSinglePlayer.drawButton(mouseX, mouseY);
        this.btnMultiplayer.drawButton(mouseX, mouseY);

        this.altList.drawList(mouseX, mouseY);

        this.btnClientOptions.drawButton(mouseX, mouseY);
        this.btnCosmetics.drawButton(mouseX, mouseY);
        this.btnMinecraftOptions.drawButton(mouseX, mouseY);
        this.btnLanguage.drawButton(mouseX, mouseY);
        this.btnForgeModList.drawButton(mouseX, mouseY);

        this.btnQuit.drawButton(mouseX, mouseY);

        String s = "Copyright Mojang Studios. Do not distribute!";
        FontUtil.TEXT.getFont().drawString(Client.clientName + Client.clientVersion + "(" + GitUtils.gitBranch + "/" + GitUtils.gitInfo.getProperty("git.commit.id.abbrev") + ") | Minecraft 1.8.9", 7, this.height - 11, new Color(255, 255, 255, 100).getRGB());
        FontUtil.TEXT.getFont().drawString(s, this.width - FontUtil.TEXT.getFont().getWidth(s) - 6, this.height - 11, new Color(255, 255, 255, 100).getRGB());
    }

    @Override
    public void updateScreen() {
        ++panoramaTimer;
        super.updateScreen();
    }

    private void drawPanorama(int p_73970_1_, int p_73970_2_, float p_73970_3_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.matrixMode(5889);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(5888);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.disableCull();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        int i = 8;

        for (int j = 0; j < i * i; ++j) {
            GlStateManager.pushMatrix();
            float f = ((float) (j % i) / (float) i - 0.5F) / 64.0F;
            float f1 = ((float) (j / i) / (float) i - 0.5F) / 64.0F;
            float f2 = 0.0F;
            GlStateManager.translate(f, f1, f2);
            GlStateManager.rotate(MathHelper.sin(((float) panoramaTimer + p_73970_3_) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-((float) panoramaTimer + p_73970_3_) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int k = 0; k < 6; ++k) {
                GlStateManager.pushMatrix();

                if (k == 1) {
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 3) {
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (k == 4) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (k == 5) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                this.mc.getTextureManager().bindTexture(titlePanoramaPaths[k]);
                worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                int l = 255 / (j + 1);
                float f3 = 0.0F;
                worldrenderer.pos(-1.0D, -1.0D, 1.0D).tex(0.0D, 0.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0D, -1.0D, 1.0D).tex(1.0D, 0.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(1.0D, 1.0D, 1.0D).tex(1.0D, 1.0D).color(255, 255, 255, l).endVertex();
                worldrenderer.pos(-1.0D, 1.0D, 1.0D).tex(0.0D, 1.0D).color(255, 255, 255, l).endVertex();
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.matrixMode(5889);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.enableDepth();
    }

    private void rotateAndBlurSkybox(float p_73968_1_) {
        GL11.glTexParameteri(3553, 10241, 9729);
        GL11.glTexParameteri(3553, 10240, 9729);
        GL11.glCopyTexSubImage2D(3553, 0, 0, 0, 0, 0, 256, 256);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.colorMask(true, true, true, false);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        GlStateManager.disableAlpha();
        int i = 3;

        for (int j = 0; j < i; ++j) {
            float f = 1.0F / (float) (j + 1);
            int k = this.width;
            int l = this.height;
            float f1 = (float) (j - i / 2) / 256.0F;
            worldrenderer.pos(k, l, this.zLevel).tex(0.0F + f1, 1.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(k, 0.0, this.zLevel).tex(1.0F + f1, 1.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0, 0.0, this.zLevel).tex(1.0F + f1, 0.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
            worldrenderer.pos(0.0, l, this.zLevel).tex(0.0F + f1, 0.0).color(1.0F, 1.0F, 1.0F, f).endVertex();
        }

        tessellator.draw();
        GlStateManager.enableAlpha();
        GlStateManager.colorMask(true, true, true, true);
    }

    public void renderSkybox(int p_73971_1_, int p_73971_2_, float p_73971_3_) {
        this.mc.getFramebuffer().unbindFramebuffer();
        GlStateManager.viewport(0, 0, 256, 256);
        this.drawPanorama(p_73971_1_, p_73971_2_, p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.rotateAndBlurSkybox(p_73971_3_);
        this.mc.getFramebuffer().bindFramebuffer(true);
        GlStateManager.viewport(0, 0, this.mc.displayWidth, this.mc.displayHeight);
        float f = this.width > this.height ? 120.0F / (float) this.width : 120.0F / (float) this.height;
        float f1 = (float) this.height * f / 256.0F;
        float f2 = (float) this.width * f / 256.0F;
        int i = this.width;
        int j = this.height;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        worldrenderer.pos(0.0, j, this.zLevel).tex(0.5F - f1, 0.5F + f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(i, j, this.zLevel).tex(0.5F - f1, 0.5F - f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(i, 0.0, this.zLevel).tex(0.5F + f1, 0.5F - f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        worldrenderer.pos(0.0, 0.0, this.zLevel).tex(0.5F + f1, 0.5F + f2).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        tessellator.draw();
    }
}
