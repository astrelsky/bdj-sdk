package org.homebrew;

import java.awt.BorderLayout;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;
import org.dvb.event.EventManager;
import org.dvb.event.UserEvent;
import org.dvb.event.UserEventListener;
import org.dvb.event.UserEventRepository;
import org.havi.ui.HScene;
import org.havi.ui.HSceneFactory;
import org.havi.ui.event.HRcEvent;

public class MyXlet extends Thread implements Xlet, UserEventListener {
    private static final int ELF_PORT = 9020;
    private static final int JAR_PORT = 9025;
    private static final int LUA_PORT = 9938;
    
    private HScene scene;
    private LoggingUI gui;

    public void initXlet(XletContext context) {
	gui = LoggingUI.getInstance();
	gui.setSize(1280, 720);

	scene = HSceneFactory.getInstance().getDefaultHScene();
	scene.add(gui, BorderLayout.CENTER);
        scene.validate();
	scene.repaint();

	UserEventRepository evtRepo = new UserEventRepository("input");
	evtRepo.addKey(HRcEvent.VK_ENTER);

	EventManager.getInstance().addUserEventListener(this, evtRepo);
	LoggingUI.getInstance().log("[*] Press X to start");
    }

    public void startXlet() {
	gui.setVisible(true);
        scene.setVisible(true);
    }

    public void pauseXlet() {
	gui.setVisible(false);
    }

    public void destroyXlet(boolean unconditional) {
	interrupt();
	gui.setVisible(false);
	scene.remove(gui);
        scene = null;
    }

    public void userEventReceived(UserEvent evt) {
	start();
    }

    public void run() {
	try {
	    LoggingUI.getInstance().log("[*] Disabling Java security manager...");
	    PrivilegeEscalation.disableSecurityManager();
	    LoggingUI.getInstance().log("[+] Java security manager disabled");

	    LoggingUI.getInstance().log("[*] Obtaining kernel .data R/W capabilities...");
	    KernelMemory.enableRW();
	    LoggingUI.getInstance().log("[+] Kernel .data R/W achieved");
	    
	    KernelPatching.enableDebugMenu();
	    LoggingUI.getInstance().log("[+] Debug menu enabled");
	    
	    KernelPatching.escalatePrivileges();
	    LoggingUI.getInstance().log("[+] Escalated privileges");

	    ElfLoadingServer.spawn(ELF_PORT);
	    LoggingUI.getInstance().log("[+] ELF loader running on port " + ELF_PORT);

 	    JarLoadingServer.spawn(JAR_PORT);
	    LoggingUI.getInstance().log("[+] JAR loader running on port " + JAR_PORT);

	    LuaLoadingServer.spawn(LUA_PORT);
	    LoggingUI.getInstance().log("[+] Lua loader running on port " + LUA_PORT);
	    
	    KernelPatching.jailbreak();
	    LoggingUI.getInstance().log("[+] Escaped FreeBSD jail");
	    LoggingUI.getInstance().log("[+] Done");
	} catch (Throwable t) {
	    LoggingUI.getInstance().log(t);
	}
    }
}

