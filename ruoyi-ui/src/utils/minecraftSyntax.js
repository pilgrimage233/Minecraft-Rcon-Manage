// Minecraft 命令关键字
export const MINECRAFT_KEYWORDS = [
  'give', 'tp', 'teleport', 'kill', 'gamemode', 'gm', 'time', 'weather',
  'difficulty', 'spawnpoint', 'setworldspawn', 'gamerule', 'xp', 'clear',
  'effect', 'enchant', 'fill', 'setblock', 'say', 'msg', 'tell', 'w',
  'scoreboard', 'execute', 'function'
];

// 选择器关键字
const SELECTORS = ['@p', '@a', '@r', '@e', '@s'];

// 游戏模式
const GAMEMODES = ['survival', 'creative', 'adventure', 'spectator'];

export function highlightMinecraftSyntax(text) {
  let result = text;

  // 处理命令前缀
  if (text.startsWith('/')) {
    result = `\x1b[33m/\x1b[0m${result.slice(1)}`;
  }

  // 高亮命令关键字
  MINECRAFT_KEYWORDS.forEach(keyword => {
    const regex = new RegExp(`\\b${keyword}\\b`, 'g');
    result = result.replace(regex, `\x1b[36m${keyword}\x1b[0m`);
  });

  // 高亮选择器
  SELECTORS.forEach(selector => {
    const regex = new RegExp(selector, 'g');
    result = result.replace(regex, `\x1b[35m${selector}\x1b[0m`);
  });

  // 高亮游戏模式
  GAMEMODES.forEach(mode => {
    const regex = new RegExp(`\\b${mode}\\b`, 'g');
    result = result.replace(regex, `\x1b[32m${mode}\x1b[0m`);
  });

  // 高亮数字
  result = result.replace(/\b\d+\b/g, match => `\x1b[33m${match}\x1b[0m`);

  // 高亮坐标
  result = result.replace(/[~^]\-?\d*\.?\d*/g, match => `\x1b[33m${match}\x1b[0m`);

  // 高亮布尔值
  result = result.replace(/\b(true|false)\b/g, match => `\x1b[35m${match}\x1b[0m`);

  return result;
} 