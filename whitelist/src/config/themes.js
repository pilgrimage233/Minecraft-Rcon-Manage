export const themes = {
    default: {
        name: '默认主题',
        gradient: 'linear-gradient(-45deg, #e0e0ff, #ffd6e7, #ffdfd0, #fff4d1, #d4ffe6, #cce9ff, #f0e6ff, #ffe6f0, #ffe6e6)',
        backgroundColor: '#ffffff',
        backgroundColorDark: '#1e1e2e',
        textColor: '#303133',
        textColorDark: '#e5e5e5',
        primaryColor: '#409EFF',
        secondaryColor: '#67C23A',
        borderColor: 'rgba(64, 158, 255, 0.2)',
        borderColorDark: 'rgba(255, 255, 255, 0.1)'
    },
    cyberpunk: {
        name: '赛博朋克',
        gradient: 'linear-gradient(-45deg, #0a0628, #100b3c, #1a0f50, #241264)',
        backgroundColor: '#0f0a2a',
        backgroundColorDark: '#080520',
        textColor: '#00ffd5',
        textColorDark: '#00fff2',
        primaryColor: '#f618f6',
        secondaryColor: '#00ffdd',
        borderColor: 'rgba(246, 24, 246, 0.3)',
        borderColorDark: 'rgba(0, 255, 221, 0.3)',
        neonShadow: '0 0 10px rgba(246, 24, 246, 0.4), 0 0 20px rgba(0, 255, 221, 0.3)',
        textShadow: '0 0 8px rgba(0, 255, 213, 0.5)',
        glowEffect: true,
        glowColor: 'rgba(0, 255, 221, 0.15)'
    },
    sakura: {
        name: '樱花',
        gradient: 'linear-gradient(-45deg, #ffd1dc, #ffe6e6, #ffb7c5, #fff0f5)',
        backgroundColor: '#fff0f5',
        backgroundColorDark: '#2a1f2d',
        textColor: '#d4317c',
        textColorDark: '#ffd1dc',
        primaryColor: '#ff69b4',
        secondaryColor: '#ffb7c5',
        borderColor: 'rgba(255, 105, 180, 0.2)',
        borderColorDark: 'rgba(255, 209, 220, 0.2)'
    },
    ocean: {
        name: '海洋',
        primary: '#1976D2',
        secondary: '#03A9F4',
        text: '#0D47A1',
        textDark: '#E3F2FD',
        bg: '#F0F8FF',
        bgDark: '#0D47A1',
        border: '#64B5F6',
        borderDark: '#1976D2',
        gradient: 'linear-gradient(120deg, #E3F2FD, #BBDEFB, #90CAF9)',
        primaryRgb: '25, 118, 210',
        secondaryRgb: '3, 169, 244'
    },
    forest: {
        name: '森林',
        gradient: 'linear-gradient(-45deg, #2d5a27, #3e8e41, #4caf50, #8bc34a)',
        backgroundColor: '#2d5a27',
        backgroundColorDark: '#1a331a',
        textColor: '#e6ffe6',
        textColorDark: '#ccffcc',
        primaryColor: '#4caf50',
        secondaryColor: '#8bc34a',
        borderColor: 'rgba(76, 175, 80, 0.2)',
        borderColorDark: 'rgba(139, 195, 74, 0.2)'
    },
    aurora: {
        name: '极光',
        primary: '#26C6DA',
        secondary: '#00BFA5',
        text: '#006064',
        textDark: '#E0F7FA',
        bg: '#E0F7FA',
        bgDark: '#006064',
        border: '#4DD0E1',
        borderDark: '#00ACC1',
        gradient: 'linear-gradient(120deg, #E0F7FA, #B2EBF2, #80DEEA, #4DD0E1, #26C6DA)',
        primaryRgb: '38, 198, 218',
        secondaryRgb: '0, 191, 165'
    }
}

export const getThemeVariables = (themeName) => {
    const theme = themes[themeName] || themes.default
    return {
        '--theme-gradient': theme.gradient,
        '--theme-bg': theme.backgroundColor,
        '--theme-bg-dark': theme.backgroundColorDark,
        '--theme-text': theme.textColor,
        '--theme-text-dark': theme.textColorDark,
        '--theme-primary': theme.primaryColor,
        '--theme-secondary': theme.secondaryColor,
        '--theme-border': theme.borderColor,
        '--theme-border-dark': theme.borderColorDark,
        '--theme-neon-shadow': theme.neonShadow || 'none',
        '--theme-text-shadow': theme.textShadow || 'none',
        '--theme-font-family': theme.fontFamily || 'Avenir, Helvetica, Arial, sans-serif',
        '--theme-font-weight': theme.fontWeight || '400',
        '--theme-letter-spacing': theme.letterSpacing || 'normal'
    }
} 