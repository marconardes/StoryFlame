const fs = require("fs");
const path = require("path");
const { execFileSync } = require("child_process");

const ELECTRON_DIR = path.resolve(__dirname, "..");
const ROOT_DIR = path.resolve(ELECTRON_DIR, "..");
const IS_WINDOWS = process.platform === "win32";
const GRADLEW = path.join(ROOT_DIR, IS_WINDOWS ? "gradlew.bat" : "gradlew");
const BRIDGE_SOURCE = path.join(
  ROOT_DIR,
  "app",
  "build",
  "install",
  "storyflame-bridge"
);
const BRIDGE_TARGET = path.join(ELECTRON_DIR, "vendor", "storyflame-bridge");

function run() {
  execFileSync(GRADLEW, [":app:installDist"], {
    cwd: ROOT_DIR,
    stdio: "inherit"
  });

  fs.rmSync(BRIDGE_TARGET, { recursive: true, force: true });
  fs.mkdirSync(path.dirname(BRIDGE_TARGET), { recursive: true });
  fs.cpSync(BRIDGE_SOURCE, BRIDGE_TARGET, { recursive: true });

  process.stdout.write(`Bridge copiado para ${BRIDGE_TARGET}\n`);
}

run();
