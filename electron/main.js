const { app, BrowserWindow, dialog, ipcMain, Menu } = require("electron");
const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");
const readline = require("readline");

const ROOT_DIR = path.resolve(__dirname, "..");
const IS_WINDOWS = process.platform === "win32";
const IS_LINUX = process.platform === "linux";
const IS_UI_DEBUG = process.env.STORYFLAME_ELECTRON_DEVTOOLS === "1";

if (IS_LINUX) {
  app.disableHardwareAcceleration();
  app.commandLine.appendSwitch("enable-logging", "stderr");
  app.commandLine.appendSwitch("log-level", "2");
  app.commandLine.appendSwitch("ozone-platform", "x11");
  app.commandLine.appendSwitch("disable-features", "UseOzonePlatform,WaylandWindowDecorations");
  app.commandLine.appendSwitch("gtk-version", "3");
}

let bridgeProcess = null;
let bridgePort = null;

function createWindow() {
  const window = new BrowserWindow({
    width: 1320,
    height: 860,
    minWidth: 980,
    minHeight: 720,
    backgroundColor: "#f5ede3",
    webPreferences: {
      preload: path.join(__dirname, "preload.js"),
      contextIsolation: true,
      nodeIntegration: false
    }
  });
  window.loadFile(path.join(__dirname, "renderer", "index.html"));
  window.maximize();

  window.webContents.on("before-input-event", (event, input) => {
    const toggleShortcut = input.key === "F12" || (input.control && input.shift && input.key.toLowerCase() === "i");
    if (!toggleShortcut) {
      return;
    }
    event.preventDefault();
    if (window.webContents.isDevToolsOpened()) {
      window.webContents.closeDevTools();
    } else {
      window.webContents.openDevTools({ mode: "detach" });
    }
  });

  window.webContents.on("context-menu", (event, params) => {
    const menu = Menu.buildFromTemplate([
      {
        label: "Inspect Element",
        click: () => {
          window.webContents.inspectElement(params.x, params.y);
          if (!window.webContents.isDevToolsOpened()) {
            window.webContents.openDevTools({ mode: "detach" });
          }
        }
      }
    ]);
    menu.popup({ window });
  });

  if (IS_UI_DEBUG) {
    window.webContents.openDevTools({ mode: "detach" });
  }
}

function resolveBridgeCommand() {
  const packagedBridge = path.join(
    process.resourcesPath,
    "storyflame-bridge",
    "bin",
    IS_WINDOWS ? "storyflame-bridge.bat" : "storyflame-bridge"
  );
  const vendoredBridge = path.join(
    __dirname,
    "vendor",
    "storyflame-bridge",
    "bin",
    IS_WINDOWS ? "storyflame-bridge.bat" : "storyflame-bridge"
  );
  const localBridge = path.join(
    ROOT_DIR,
    "app",
    "build",
    "install",
    "storyflame-bridge",
    "bin",
    IS_WINDOWS ? "storyflame-bridge.bat" : "storyflame-bridge"
  );

  if (app.isPackaged && fs.existsSync(packagedBridge)) {
    return {
      command: packagedBridge,
      args: ["--port=0"]
    };
  }
  if (fs.existsSync(localBridge)) {
    return {
      command: localBridge,
      args: ["--port=0"]
    };
  }
  if (fs.existsSync(vendoredBridge)) {
    return {
      command: vendoredBridge,
      args: ["--port=0"]
    };
  }
  return {
    command: IS_WINDOWS ? "gradlew.bat" : "./gradlew",
    args: [":app:runElectronBridge", "--args=--port=0"]
  };
}

function startBridge() {
  return new Promise((resolve, reject) => {
    const bridge = resolveBridgeCommand();
    const child = spawn(bridge.command, bridge.args, {
      cwd: ROOT_DIR,
      stdio: ["ignore", "pipe", "pipe"]
    });
    bridgeProcess = child;

    const stdout = readline.createInterface({ input: child.stdout });
    const stderr = readline.createInterface({ input: child.stderr });

    stdout.on("line", (line) => {
      const match = line.match(/STORYFLAME_ELECTRON_BRIDGE_PORT=(\d+)/);
      if (match) {
        bridgePort = Number(match[1]);
        resolve(bridgePort);
      } else {
        console.log(`[storyflame-bridge] ${line}`);
      }
    });

    stderr.on("line", (line) => {
      console.error(`[storyflame-bridge] ${line}`);
    });

    child.once("error", reject);
    child.once("exit", (code) => {
      if (bridgePort == null) {
        reject(new Error(`Bridge finalizado antes de iniciar. Codigo ${code}`));
      }
    });
  });
}

ipcMain.handle("storyflame:get-bridge-port", async () => bridgePort);

ipcMain.handle("storyflame:open-project-dialog", async () => {
  const result = await dialog.showOpenDialog({
    title: "Abrir projeto StoryFlame",
    properties: ["openFile"],
    filters: [{ name: "StoryFlame", extensions: ["storyflame"] }]
  });
  if (result.canceled || result.filePaths.length === 0) {
    return null;
  }
  return result.filePaths[0];
});

app.whenReady().then(async () => {
  try {
    await startBridge();
    createWindow();
  } catch (error) {
    await dialog.showMessageBox({
      type: "error",
      title: "Falha ao iniciar o StoryFlame Electron",
      message: "Nao foi possivel iniciar o bridge local do StoryFlame.",
      detail: String(error && error.message ? error.message : error)
    });
    app.quit();
  }
});

app.on("window-all-closed", () => {
  if (bridgeProcess) {
    bridgeProcess.kill();
    bridgeProcess = null;
  }
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("before-quit", () => {
  if (bridgeProcess) {
    bridgeProcess.kill();
    bridgeProcess = null;
  }
});
