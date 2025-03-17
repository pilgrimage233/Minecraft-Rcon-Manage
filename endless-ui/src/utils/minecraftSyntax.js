// Minecraft 命令关键字
export const MINECRAFT_KEYWORDS = [
  // 基础命令
  'give', 'tp', 'teleport', 'kill', 'gamemode', 'gm', 'time', 'weather',
  'difficulty', 'spawnpoint', 'setworldspawn', 'gamerule', 'xp', 'clear',
  'effect', 'enchant', 'fill', 'setblock', 'say', 'msg', 'tell', 'w',
  'scoreboard', 'execute', 'function', 'seed', 'help', 'list',

  // 游戏规则和世界设置
  'defaultgamemode', 'setworldspawn', 'worldborder', 'particle',
  'playsound', 'stopsound', 'title', 'trigger', 'whitelist',
  'ban', 'ban-ip', 'banlist', 'kick', 'pardon', 'pardon-ip',

  // 实体和玩家管理
  'team', 'spectate', 'spreadplayers', 'summon', 'tag', 'data',
  'attribute', 'bossbar', 'experience', 'advancement', 'recipe',

  // 区块和结构
  'clone', 'forceload', 'locate', 'structure', 'place',

  // 调试和开发
  'debug', 'datapack', 'reload', 'save-all', 'save-off', 'save-on',
  'stop', 'tellraw', 'me', 'deop', 'op', 'publish',

  // 物品和库存
  'item', 'loot', 'replaceitem', 'clear', 'give', 'drop',

  // 计分板
  'scoreboard objectives', 'scoreboard players', 'scoreboard teams',

  // 特殊效果
  'particle', 'playsound', 'stopsound', 'title', 'subtitle',

  // 服务器管理
  'ban', 'kick', 'whitelist', 'op', 'deop', 'save-all',
  'save-on', 'save-off', 'stop', 'restart', 'tps', 'mspt',

  // 世界管理
  'difficulty', 'gamerule', 'weather', 'time', 'worldborder',
  'spawnpoint', 'setworldspawn', 'seed', 'setidletimeout'
];

// 选择器关键字
const SELECTORS = [
  '@p', '@a', '@r', '@e', '@s',
  '@e\\[type=', '@e\\[distance=', '@e\\[limit=', '@a\\[gamemode=',
  '@e\\[sort=', '@e\\[tag=', '@e\\[team=', '@e\\[scores={'
];

// 游戏模式
const GAMEMODES = [
  'survival', 'creative', 'adventure', 'spectator',
  '0', '1', '2', '3'  // 游戏模式的数字表示
];

export function highlightMinecraftSyntax(text) {
  let result = text;

  // 处理命令前缀
  if (text.startsWith('/')) {
    result = `\x1b[33m/\x1b[0m${result.slice(1)}`;
  }

  // 高亮选择器
  const selectorPattern = /@[apers](\[.*?\])?/g;
  result = result.replace(selectorPattern, match => `\x1b[35m${match}\x1b[0m`);

  // 高亮命令关键字
  MINECRAFT_KEYWORDS.forEach(keyword => {
    const regex = new RegExp(`\\b${keyword}\\b`, 'g');
    result = result.replace(regex, `\x1b[36m${keyword}\x1b[0m`);
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
