package extracells;

import appeng.api.AEApi;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import extracells.network.ChannelHandler;
import extracells.network.GuiHandler;
import extracells.proxy.CommonProxy;
import extracells.registries.ItemEnum;
import extracells.render.RenderHandler;
import extracells.util.FluidCellHandler;
import extracells.util.NameHandler;
import extracells.util.ExtraCellsEventHandler;
import extracells.wireless.AEWirelessTermHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

@Mod(modid = "extracells", name = "Extra Cells", dependencies = "after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2")
public class Extracells {

    @Instance("extracells")
    public static Extracells instance;

    @SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static String VERSION;

    private static File configFolder;
    public static boolean shortenedBuckets;
    public static boolean dynamicTypes;
    public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells") {

        public ItemStack getIconItemStack() {
            return new ItemStack(ItemEnum.FLUIDSTORAGE.getItem());
        }

        @Override
        public Item getTabIconItem() {
            return ItemEnum.FLUIDSTORAGE.getItem();
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        instance = this;
        VERSION = Loader.instance().activeModContainer().getVersion();
        configFolder = event.getModConfigurationDirectory();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        // Config
        Configuration config = new Configuration(new File(configFolder.getPath() + File.separator + "AppliedEnergistics2" + File.separator + "extracells.cfg"));
        config.load();
        shortenedBuckets = config.get("Tooltips", "shortenedBuckets", true, "Shall the guis shorten large mB values?").getBoolean(true);
        dynamicTypes = config.get("Storage Cells", "dynamicTypes", true, "Should the mount of bytes needed for a new type depend on the cellsize?").getBoolean(true);
        config.save();

        proxy.registerItems();
        proxy.registerBlocks();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        AEApi.instance().registries().recipes().addNewSubItemResolver(new NameHandler());
        AEApi.instance().registries().wireless().registerWirelessHandler(new AEWirelessTermHandler());
        AEApi.instance().registries().cell().addCellHandler(new FluidCellHandler());
        ExtraCellsEventHandler handler = new ExtraCellsEventHandler();
        FMLCommonHandler.instance().bus().register(handler);
        MinecraftForge.EVENT_BUS.register(handler);
        proxy.registerMovables();
        proxy.registerRenderers();
        proxy.registerTileEntities();
        proxy.addRecipes(configFolder);
        ChannelHandler.registerMessages();
        RenderingRegistry.registerBlockHandler(new RenderHandler(RenderingRegistry.getNextAvailableRenderId()));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}
