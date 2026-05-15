package top.ribs.scguns.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.Config;
import top.ribs.scguns.Reference;
import top.ribs.scguns.client.handler.GunRenderingHandler;
import top.ribs.scguns.client.screen.widget.MiniButton;
import top.ribs.scguns.client.util.RenderUtil;
import top.ribs.scguns.common.FireMode;
import top.ribs.scguns.common.GripType;
import top.ribs.scguns.common.Gun;
import top.ribs.scguns.common.ReloadType;
import top.ribs.scguns.common.container.AttachmentContainer;
import top.ribs.scguns.common.container.slot.AttachmentSlot;
import top.ribs.scguns.item.GunItem;
import top.ribs.scguns.item.attachment.IAttachment;
import top.ribs.scguns.util.GunCompositeStatHelper;
import top.ribs.scguns.util.GunEnchantmentHelper;
import top.ribs.scguns.util.GunModifierHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Author: MrCrayfish
 */
public class AttachmentScreen extends AbstractContainerScreen<AttachmentContainer>
{
    private static final ResourceLocation GUI_TEXTURES = ResourceLocation.parse("scguns:textures/gui/attachments.png");
    private static final Component CONFIG_TOOLTIP = Component.translatable("scguns.button.config.tooltip");

    private final Inventory playerInventory;
    private final Container weaponInventory;

    private boolean showHelp = true;
    private int windowZoom = 10;
    private int windowX, windowY;
    private float windowRotationX, windowRotationY;
    private boolean mouseGrabbed;
    private int mouseGrabbedButton;
    private int mouseClickedX, mouseClickedY;

    public AttachmentScreen(AttachmentContainer screenContainer, Inventory playerInventory, Component titleIn)
    {
        super(screenContainer, playerInventory, titleIn);
        this.playerInventory = playerInventory;
        this.weaponInventory = screenContainer.getWeaponInventory();
        this.imageWidth = 188;
        this.imageHeight = 192;
    }

    @Override
    protected void init()
    {
        super.init();

        this.gatherButtons().forEach(this::addRenderableWidget);
    }

    private List<MiniButton> gatherButtons()
    {
        List<MiniButton> buttons = new ArrayList<>();
        return buttons;
    }

    @Override
    public void containerTick()
    {
        super.containerTick();
        if(this.minecraft != null && this.minecraft.player != null)
        {
            if(!(this.minecraft.player.getMainHandItem().getItem() instanceof GunItem))
            {
                Minecraft.getInstance().setScreen(null);
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(pGuiGraphics, mouseX, mouseY, partialTicks);
        super.render(pGuiGraphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(pGuiGraphics, mouseX, mouseY); //Render tool tips

        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;
        int numSlots = IAttachment.Type.values().length;
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;

        for(int i = 0; i < numSlots; i++)
        {
            int x = centerX + i * 18;
            int y = 89;
            if(RenderUtil.isMouseWithin(mouseX, mouseY, startX + x, startY + y, 18, 18))
            {
                IAttachment.Type type = IAttachment.Type.values()[i];
                if(!this.menu.getSlot(i).isActive())
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.scguns.attachment." + type.getTranslationKey()), Component.translatable("slot.scguns.attachment.not_applicable")), mouseX, mouseY);
                }
                else if(this.menu.getSlot(i) instanceof AttachmentSlot slot && slot.getItem().isEmpty() && !this.isCompatible(this.menu.getCarried(), slot))
                {
                    pGuiGraphics.renderComponentTooltip(this.font, Arrays.asList(Component.translatable("slot.scguns.attachment.incompatible").withStyle(ChatFormatting.YELLOW)), mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        Minecraft minecraft = Minecraft.getInstance();

        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;
        pGuiGraphics.enableScissor(left - 6, top - 65, left + 210, top + 88);
        pGuiGraphics.pose().pushPose();
        pGuiGraphics.pose().translate(96, 48, 150);
        pGuiGraphics.pose().translate(this.windowX + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseX - this.mouseClickedX : 0), 0, 0);
        pGuiGraphics.pose().translate(0, this.windowY + (this.mouseGrabbed && this.mouseGrabbedButton == 0 ? mouseY - this.mouseClickedY : 0), 0);
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(-30F));
        pGuiGraphics.pose().mulPose(Axis.XP.rotationDegrees(this.windowRotationY - (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseY - this.mouseClickedY : 0)));
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(this.windowRotationX + (this.mouseGrabbed && this.mouseGrabbedButton == 1 ? mouseX - this.mouseClickedX : 0)));
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(150F));
        pGuiGraphics.pose().scale(this.windowZoom / 10F, this.windowZoom / 10F, this.windowZoom / 10F);
        pGuiGraphics.pose().mulPose(Axis.YP.rotationDegrees(90F));
        pGuiGraphics.pose().mulPose((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        pGuiGraphics.pose().scale(90.0F, 90.0F, 90.0F);
        var modelStack = RenderSystem.getModelViewStack();
        modelStack.pushMatrix();
        modelStack.mul(pGuiGraphics.pose().last().pose());
        RenderSystem.applyModelViewMatrix();
        assert this.minecraft != null;
        MultiBufferSource.BufferSource buffer = this.minecraft.renderBuffers().bufferSource();
        assert this.minecraft.player != null;
        GunRenderingHandler.get().renderWeapon(this.minecraft.player, this.minecraft.player.getMainHandItem(), ItemDisplayContext.GROUND, new PoseStack(), buffer, 15728880, 0F);
        buffer.endBatch();
        pGuiGraphics.pose().popPose();
        modelStack.popMatrix();
        RenderSystem.applyModelViewMatrix();
        pGuiGraphics.disableScissor();

        pGuiGraphics.flush();
        this.renderGunStats(pGuiGraphics);
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int left = (this.width - this.imageWidth) / 2 - 6;
        int top = (this.height - this.imageHeight) / 2 + 19;
        pGuiGraphics.blit(GUI_TEXTURES, left, top, 0, 0, this.imageWidth, this.imageHeight);

        /* Draws the icons for each attachment slot. If not applicable
         * for the weapon, it will draw a cross instead. */
        int numSlots = IAttachment.Type.values().length;
        int centerX = 88 - (numSlots * 18) / 2 + 18 - 5;
        for (int i = 0; i < numSlots; i++) {
            int x = centerX + i * 18;
            int y = 89;
            if (!this.menu.getSlot(i).isActive()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 192, 0, 16, 16);
            } else if (this.weaponInventory.getItem(i).isEmpty()) {
                pGuiGraphics.blit(GUI_TEXTURES, left + x, top + y, 192, 16 + i * 16, 16, 16);
            }
        }
    }

    private void renderGunStats(GuiGraphics graphics) {
        if (this.minecraft == null || this.minecraft.player == null) {
            return;
        }

        ItemStack gunStack = this.minecraft.player.getMainHandItem();
        if (!(gunStack.getItem() instanceof GunItem gunItem)) {
            return;
        }

        Gun modifiedGun = gunItem.getModifiedGun(gunStack);
        Gun.Projectile projectile = Gun.getDisplayProjectile(gunStack, modifiedGun);
        Gun.General general = modifiedGun.getGeneral();
        Gun.Reloads reloads = modifiedGun.getReloads();

        int startX = -60;
        int startY = 0;
        int lineHeight = 7;
        float scale = 0.6F;

        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, scale);

        int scaledX = (int) (startX / scale);
        int currentY = (int) (startY / scale);

        String gunName = Component.translatable(gunStack.getDescriptionId()).getString();
        graphics.drawString(this.font, gunName, scaledX, currentY, 0xFFFFFF, false);
        graphics.fill(scaledX, currentY + 8, scaledX + this.font.width(gunName), currentY + 9, 0xFFFFFFFF);
        currentY += (int) ((lineHeight + 3) / scale);

        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.damage").getString(), formatOne(getDisplayDamage(gunStack, projectile)));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.critical_chance").getString(), formatOne(GunModifierHelper.getCriticalChance(gunStack) * 100.0F) + "%");
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.critical_multiplier").getString(), formatOne(Config.COMMON.gameplay.criticalDamageMultiplier.get().floatValue()) + "x");
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.armor_penetration").getString(), formatOne(GunEnchantmentHelper.getPuncturingArmorBypass(gunStack)));
        currentY += (int) (lineHeight / scale);

        FireMode fireMode = general.getFireMode();
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.fire_mode").getString(), Component.translatable("fire_mode." + fireMode.id()).getString());
        currentY += (int) (lineHeight / scale);

        int rate = Math.max(GunCompositeStatHelper.getCompositeRate(gunStack, modifiedGun), 1);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.fire_rate").getString(), String.format(Locale.ROOT, "%.0f RPM", (20.0F / rate) * 60.0F));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.max_ammo").getString(), Integer.toString(GunModifierHelper.getModifiedAmmoCapacity(gunStack, modifiedGun)));
        currentY += (int) (lineHeight / scale);

        ReloadType reloadType = reloads.getReloadType();
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.reload_type").getString(), Component.translatable("reload_type." + reloadType.id().toString().replace(":", ".")).getString());
        currentY += (int) (lineHeight / scale);

        GripType gripType = modifiedGun.determineGripType(gunStack);
        String gripKey = gripType == GripType.ONE_HANDED ? "info.scguns.grip_one_handed" : "info.scguns.grip_two_handed";
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.grip_type").getString(), Component.translatable(gripKey).getString());
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.spread").getString(), formatTwo(GunModifierHelper.getModifiedSpread(gunStack, general.getSpread())));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.recoil_angle").getString(), formatOne(general.getRecoilAngle()));
        currentY += (int) (lineHeight / scale);
        drawStatLine(graphics, scaledX, currentY, Component.translatable("info.scguns.falloff_range").getString(), Component.translatable("info.scguns.falloff_none").getString());

        graphics.pose().popPose();
    }

    private static void drawStatLine(GuiGraphics graphics, int x, int y, String label, String value) {
        graphics.drawString(Minecraft.getInstance().font, label + ": " + value, x, y, 0xFFFFFF, false);
    }

    private static float getDisplayDamage(ItemStack gunStack, Gun.Projectile projectile) {
        float damage = projectile.getDamage();
        damage = GunModifierHelper.getModifiedProjectileDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getAcceleratorDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getHeavyShotDamage(gunStack, damage);
        damage = GunEnchantmentHelper.getPuncturingDamageReductionForTooltip(gunStack, damage);
        damage *= Config.COMMON.gameplay.globalDamageMultiplier.get().floatValue();

        CompoundTag tag = getStackTag(gunStack);
        if (tag != null && tag.contains("AdditionalDamage", Tag.TAG_ANY_NUMERIC)) {
            damage += tag.getFloat("AdditionalDamage");
        }
        damage += GunModifierHelper.getAdditionalDamage(gunStack, false);
        return damage;
    }

    private static CompoundTag getStackTag(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && !data.isEmpty() ? data.copyTag() : null;
    }

    private static String formatOne(float value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatTwo(float value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private boolean isCompatible(ItemStack stack, AttachmentSlot slot)
    {
        if(stack.isEmpty())
            return true;


        if(!(stack.getItem() instanceof IAttachment<?> attachment))
            return false;

        if(!attachment.getType().equals(slot.getType()))
            return true;

        if(this.minecraft == null || this.minecraft.player == null)
            return false;

        if(!attachment.canAttachTo(this.minecraft.player.getMainHandItem()))
            return false;

        return slot.isActive();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;
        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX - 6, startY - 65, 216, 153))
        {
            if(scrollY < 0 && this.windowZoom > 0)
            {
                this.showHelp = false;
                this.windowZoom--;
            }
            else if(scrollY > 0)
            {
                this.showHelp = false;
                this.windowZoom++;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        int startX = (this.width - this.imageWidth) / 2 - 6;
        int startY = (this.height - this.imageHeight) / 2 + 19;

        if(RenderUtil.isMouseWithin((int) mouseX, (int) mouseY, startX - 6, startY - 65, 216, 153))
        {
            if(!this.mouseGrabbed && (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT))
            {
                this.mouseGrabbed = true;
                this.mouseGrabbedButton = button == GLFW.GLFW_MOUSE_BUTTON_RIGHT ? 1 : 0;
                this.mouseClickedX = (int) mouseX;
                this.mouseClickedY = (int) mouseY;
                this.showHelp = false;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if(this.mouseGrabbed)
        {
            if(this.mouseGrabbedButton == 0 && button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                this.mouseGrabbed = false;
                this.windowX += (int) (mouseX - this.mouseClickedX - 1);
                this.windowY += (int) (mouseY - this.mouseClickedY);
            }
            else if(mouseGrabbedButton == 1 && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                this.mouseGrabbed = false;
                this.windowRotationX += (float) (mouseX - this.mouseClickedX);
                this.windowRotationY -= (float) (mouseY - this.mouseClickedY);
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void openConfigScreen()
    {
        ModList.get().getModContainerById(Reference.MOD_ID).ifPresent(container ->
        {
            Screen screen = container.getCustomExtension(IConfigScreenFactory.class).map(factory -> factory.createScreen(container, this)).orElse(null);
            if(screen != null)
            {
                this.minecraft.setScreen(screen);
            }
            else if(this.minecraft != null && this.minecraft.player != null)
            {
                MutableComponent modName = Component.literal("Configured");
                modName.setStyle(modName.getStyle()
                        .withColor(ChatFormatting.YELLOW)
                        .withUnderlined(true)
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("scguns.chat.open_curseforge_page")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/configured")));
                Component message = Component.translatable("scguns.chat.install_configured", modName);
                this.minecraft.player.displayClientMessage(message, false);
            }
        });
    }
}
