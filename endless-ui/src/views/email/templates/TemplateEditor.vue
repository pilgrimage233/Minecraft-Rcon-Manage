<template>
  <el-dialog :title="title" :visible="visible" append-to-body width="800px"
             @close="handleClose" @update:visible="val => $emit('update:visible', val)">
    <el-tabs v-model="activeTab" type="card" @tab-click="handleTabClick">
      <el-tab-pane label="编辑" name="edit">
        <el-form ref="form" :model="form" :rules="rules" label-width="100px">
          <el-form-item label="服务器ID" prop="serverId">
            <el-select v-model="form.serverId" clearable placeholder="请选择服务器">
              <el-option :value="null" label="未指定"></el-option>
              <el-option
                v-for="item in serverOptions"
                :key="item.id"
                :label="item.nameTag"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="模板类型">
            <el-select v-model="currentTemplateType" placeholder="请选择模板类型" @change="handleTemplateTypeChange">
              <el-option label="审核" value="reviewTemp"></el-option>
              <el-option label="待审核" value="pendingTemp"></el-option>
              <el-option label="通过" value="passTemp"></el-option>
              <el-option label="拒绝" value="refuseTemp"></el-option>
              <el-option label="移除" value="removeTemp"></el-option>
              <el-option label="封禁" value="banTemp"></el-option>
              <el-option label="解禁" value="pardonTemp"></el-option>
              <el-option label="邮箱验证" value="verifyTemp"></el-option>
              <el-option label="系统告警" value="warningTemp"></el-option>
            </el-select>
          </el-form-item>

          <el-form-item v-if="currentTemplateType" label="模板内容">
            <el-input
              v-model="form[currentTemplateType]"
              :rows="15"
              placeholder="请输入HTML模板内容"
              style="font-family: 'Courier New', monospace;"
              type="textarea"
            />
          </el-form-item>

          <el-form-item label="备注" prop="remark">
            <el-input v-model="form.remark" placeholder="请输入备注" type="textarea"/>
          </el-form-item>
          <el-form-item label="状态">
            <el-radio-group v-model="form.status">
              <el-radio
                v-for="item in [{label: '启用', value: 1}, {label: '停用', value: 0}]"
                :key="item.value"
                :label="item.value"
              >{{ item.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="预览" name="preview">
        <div class="preview-container">
          <div class="template-info">
            <h4>模板类型: {{ getTemplateTypeName(previewTemplateType) }}</h4>
            <h4>服务器ID: {{ getServerName(form.serverId) }}</h4>
            <div class="preview-warning">
              <i class="el-icon-warning"></i>
              <span>此预览功能可能显示异常，仅供参考，不代表实际效果</span>
            </div>
            <div class="security-level-control">
              <span>安全级别: </span>
              <el-radio-group v-model="securityLevel" size="mini" @change="handleSecurityLevelChange">
                <el-radio-button label="strict">严格</el-radio-button>
                <el-radio-button label="relaxed">宽松</el-radio-button>
                <el-radio-button label="permissive">完全允许</el-radio-button>
                <el-radio-button label="raw">完全原始</el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <div v-if="previewTemplateType" class="preview-content">
            <iframe
              v-if="form[previewTemplateType]"
              ref="previewIframe"
              :srcdoc="getIframeContent()"
              class="template-preview-frame"
              sandbox="allow-same-origin allow-scripts allow-forms"
            ></iframe>
            <div v-else class="template-preview-placeholder">
              请选择要预览的模板类型
            </div>
          </div>

          <div class="template-selector">
            <el-select v-model="previewTemplateType" placeholder="选择预览模板" @change="handlePreviewTypeChange">
              <el-option label="审核" value="reviewTemp"></el-option>
              <el-option label="待审核" value="pendingTemp"></el-option>
              <el-option label="通过" value="passTemp"></el-option>
              <el-option label="拒绝" value="refuseTemp"></el-option>
              <el-option label="移除" value="removeTemp"></el-option>
              <el-option label="封禁" value="banTemp"></el-option>
              <el-option label="解禁" value="pardonTemp"></el-option>
              <el-option label="邮箱验证" value="verifyTemp"></el-option>
              <el-option label="系统告警" value="warningTemp"></el-option>
            </el-select>
            <el-button
              icon="el-icon-view"
              size="mini"
              style="margin-left: 10px;"
              type="primary"
              @click="openInNewWindow"
            >
              新窗口预览
            </el-button>
            <el-button
              icon="el-icon-download"
              size="mini"
              style="margin-left: 5px;"
              type="success"
              @click="downloadHtml"
            >
              下载HTML
            </el-button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button @click="downloadDocument">下载使用文档</el-button>
      <el-button v-if="activeTab === 'edit'" type="primary" @click="submitForm">确 定</el-button>
    </div>
  </el-dialog>
</template>

<script>
import {addTemplates, downloadDocument, updateTemplates} from "@/api/email/templates";
import {getServerList} from "@/api/regular/command";
import DOMPurify from "dompurify";

export default {
  name: "TemplateEditor",
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    title: {
      type: String,
      default: ""
    },
    formData: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      activeTab: "edit",
      currentTemplateType: "reviewTemp",
      previewTemplateType: "reviewTemp",
      serverOptions: [],
      // 安全级别配置：'strict'(严格) | 'relaxed'(宽松) | 'permissive'(完全允许) | 'raw'(完全原始)
      securityLevel: 'relaxed',
      form: {
        id: null,
        serverId: null,
        reviewTemp: null,
        pendingTemp: null,
        passTemp: null,
        refuseTemp: null,
        removeTemp: null,
        banTemp: null,
        pardonTemp: null,
        verifyTemp: null,
        warningTemp: null,
        remark: null
      },
      rules: {
        serverId: [
          {required: false, message: "服务器ID可以为空", trigger: "blur"}
        ]
      }
    };
  },
  created() {
    this.getServerList();
  },
  mounted() {
    // 组件挂载后的一些初始化操作
    // 确保previewTemplateType有默认值
    if (!this.previewTemplateType) {
      this.previewTemplateType = "reviewTemp";
    }

    this.$nextTick(() => {
      this.updateIframeContent();
    });
  },
  watch: {
    formData: {
      handler(val) {
        if (val) {
          this.form = {...val};
          // 表单数据更新后，确保iframe内容也更新
          this.$nextTick(() => {
            this.updateIframeContent();
          });
        }
      },
      immediate: true
    },
    'form[previewTemplateType]': {
      handler(val) {
        // 当预览模板内容变化时，更新iframe内容
        console.log('预览模板内容变化:', val);
        this.$nextTick(() => {
          this.updateIframeContent();
        });
      },
      immediate: true
    },
    currentTemplateType: {
      handler(val) {
        // 当前模板类型变化时，更新iframe内容
        console.log('当前模板类型变化:', val);
        this.$nextTick(() => {
          this.updateIframeContent();
        });
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit("update:visible", false);
      this.resetForm();
    },
    resetForm() {
      this.form = {
        id: null,
        serverId: null,
        reviewTemp: null,
        pendingTemp: null,
        passTemp: null,
        refuseTemp: null,
        removeTemp: null,
        banTemp: null,
        pardonTemp: null,
        verifyTemp: null,
        warningTemp: null,
        remark: null
      };
      this.currentTemplateType = "reviewTemp";
      this.previewTemplateType = "reviewTemp";
      this.activeTab = "edit";
    },
    handleTemplateTypeChange(val) {
      console.log('模板类型变化:', val);
      this.currentTemplateType = val;
    },
    handlePreviewTypeChange(val) {
      console.log('预览类型变化:', val);
      this.previewTemplateType = val || "reviewTemp";  // 如果val为空，则默认为"reviewTemp"
      // 确保在预览类型变化后更新iframe
      this.$nextTick(() => {
        this.updateIframeContent();
      });
    },
    handleTabClick(tab) {
      console.log('标签页切换:', tab.name);
      if (tab.name === 'preview') {
        // 切换到预览标签页时，延迟更新iframe内容
        this.$nextTick(() => {
          setTimeout(() => {
            this.updateIframeContent();
          }, 100);
        });
      }
    },
    handleSecurityLevelChange(val) {
      console.log('安全级别切换为:', val);
      // 安全级别变化时，重新更新iframe内容
      this.$nextTick(() => {
        this.updateIframeContent();
      });
    },
    getTemplateTypeName(type) {
      const typeMap = {
        reviewTemp: "审核",
        pendingTemp: "待审核",
        passTemp: "通过",
        refuseTemp: "拒绝",
        removeTemp: "移除",
        banTemp: "封禁",
        pardonTemp: "解禁",
        verifyTemp: "邮箱验证",
        warningTemp: "系统告警"
      };
      return typeMap[type] || "";
    },
    getServerList() {
      getServerList().then(response => {
        this.serverOptions = response.data;
      });
    },
    getServerName(serverId) {
      if (!serverId) {
        return "未指定";
      }
      const server = this.serverOptions.find(item => item.id === serverId);
      return server ? server.nameTag : "未知服务器";
    },
    sanitizeHtml(html) {
      // 使用DOMPurify净化HTML，防止XSS攻击
      // 根据安全级别选择不同的净化策略

      if (this.securityLevel === 'permissive') {
        // 完全允许模式 - 只禁止最危险的标签
        console.log('使用完全允许模式');
        return DOMPurify.sanitize(html, {
          FORBID_TAGS: ['script'],
          FORBID_ATTR: ['onerror', 'onload'],
          ALLOW_DATA_ATTR: true,
          ALLOW_UNKNOWN_PROTOCOLS: true,
          KEEP_CONTENT: true
        });
      } else if (this.securityLevel === 'strict') {
        // 严格模式 - 只允许基本的HTML标签
        console.log('使用严格模式');
        return DOMPurify.sanitize(html, {
          ALLOWED_TAGS: ['div', 'span', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6', 'strong', 'em', 'b', 'i', 'u', 'br', 'hr', 'a', 'img', 'table', 'tr', 'td', 'th', 'ul', 'ol', 'li'],
          ALLOWED_ATTR: ['class', 'style', 'href', 'src', 'alt', 'width', 'height', 'colspan', 'rowspan'],
          KEEP_CONTENT: true
        });
      } else {
        // 宽松模式 - 允许大部分HTML标签和属性
        console.log('使用宽松模式');
        return DOMPurify.sanitize(html, {
          ADD_TAGS: [
            // 结构标签
            'html', 'head', 'body', 'title', 'meta', 'link', 'style',
            // 文本标签
            'div', 'span', 'p', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
            'strong', 'em', 'b', 'i', 'u', 's', 'strike', 'del', 'ins',
            'mark', 'small', 'big', 'sub', 'sup', 'code', 'pre', 'kbd', 'samp',
            'var', 'cite', 'q', 'abbr', 'acronym', 'time', 'address',
            // 列表标签
            'ul', 'ol', 'li', 'dl', 'dt', 'dd',
            // 表格标签
            'table', 'thead', 'tbody', 'tfoot', 'tr', 'th', 'td', 'caption', 'colgroup', 'col',
            // 媒体标签
            'img', 'video', 'audio', 'source', 'track', 'canvas', 'svg', 'figure', 'figcaption',
            // 表单标签（只读）
            'form', 'input', 'textarea', 'select', 'option', 'optgroup', 'button', 'label', 'fieldset', 'legend',
            // 其他标签
            'a', 'br', 'hr', 'wbr', 'details', 'summary', 'main', 'section', 'article', 'aside',
            'header', 'footer', 'nav', 'menu', 'menuitem', 'blockquote', 'iframe', 'embed', 'object'
          ],
          ADD_ATTR: [
            // 通用属性
            'id', 'class', 'style', 'title', 'lang', 'dir', 'data-*',
            // 链接属性
            'href', 'target', 'rel', 'download', 'ping',
            // 媒体属性
            'src', 'alt', 'width', 'height', 'poster', 'preload', 'controls', 'autoplay', 'loop', 'muted',
            // 表格属性
            'colspan', 'rowspan', 'headers', 'scope', 'cellpadding', 'cellspacing', 'border', 'bgcolor',
            'align', 'valign', 'char', 'charoff',
            // 表单属性
            'name', 'value', 'type', 'placeholder', 'required', 'readonly', 'disabled', 'checked', 'selected',
            'multiple', 'size', 'maxlength', 'min', 'max', 'step', 'pattern', 'autocomplete', 'autofocus',
            // 元数据属性
            'charset', 'content', 'http-equiv', 'media', 'sizes', 'crossorigin',
            // 其他属性
            'tabindex', 'accesskey', 'draggable', 'dropzone', 'hidden', 'spellcheck', 'translate'
          ],
          FORBID_TAGS: [
            // 只禁止最危险的标签
            'script'
          ],
          FORBID_ATTR: [
            // 只禁止最危险的事件属性
            'onerror', 'onload'
          ],
          // 允许自定义元素
          CUSTOM_ELEMENT_HANDLING: {
            tagNameCheck: null,
            attributeNameCheck: null,
            allowCustomizedBuiltInElements: true
          },
          // 允许data属性
          ALLOW_DATA_ATTR: true,
          // 允许未知协议
          ALLOW_UNKNOWN_PROTOCOLS: true,
          // 保留注释
          KEEP_CONTENT: true
        });
      }
    },
    getIframeContent() {
      // 直接生成完整的HTML内容，不经过DOMPurify过滤
      if (!this.form[this.previewTemplateType]) return '';

      let html = this.form[this.previewTemplateType];
      console.log('原始HTML内容:', html);

      // 检查HTML是否被转义，如果是则先解码
      if (html.includes('&lt;') || html.includes('&gt;') || html.includes('&amp;')) {
        console.log('检测到HTML被转义，进行解码');
        html = html
          .replace(/&lt;/g, '<')
          .replace(/&gt;/g, '>')
          .replace(/&amp;/g, '&')
          .replace(/&quot;/g, '"')
          .replace(/&#x27;/g, "'");
        console.log('解码后的HTML:', html);
      }

      // 如果是完全原始模式，直接返回用户的HTML
      if (this.securityLevel === 'raw') {
        console.log('使用完全原始模式');
        return html;
      }

      // 提取用户的自定义样式
      const userStyles = this.extractUserStyles(html);
      console.log('提取的用户样式:', userStyles);

      // 构建完整的HTML文档，确保自定义样式优先级最高
      const fullHtml = `
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>邮件模板预览</title>
    <style>
        /* 重置所有样式，确保自定义样式生效 */
        * {
            box-sizing: border-box;
        }

        /* 用户的自定义样式 - 最高优先级 */
        ${userStyles}

        /* 基础样式增强 */
        body {
            margin: 0;
            padding: 0;
            font-family: 'Helvetica Neue', Arial, sans-serif;
            background-color: #f5f5f5;
            line-height: 1.6;
            color: #333;
        }

        /* 通用样式增强 */
        p {
            margin: 10px 0;
        }

        a {
            color: inherit;
        }

        /* 确保表格样式正确 */
        table {
            border-collapse: collapse;
            width: 100%;
            margin: 10px 0;
        }

        th, td {
            border: 1px solid #ddd;
            padding: 8px 12px;
            text-align: left;
        }

        th {
            background-color: #f5f5f5;
            font-weight: bold;
        }

        /* 确保图片响应式 */
        img {
            max-width: 100%;
            height: auto;
        }

        /* 确保列表样式 */
        ul, ol {
            padding-left: 20px;
        }

        li {
            margin: 5px 0;
        }

        /* 确保标题样式 */
        h1, h2, h3, h4, h5, h6 {
            margin: 20px 0 10px 0;
            color: inherit;
        }

        h1 { font-size: 2em; }
        h2 { font-size: 1.5em; }
        h3 { font-size: 1.3em; }
        h4 { font-size: 1.1em; }
        h5 { font-size: 1em; }
        h6 { font-size: 0.9em; }

        /* 确保代码块样式 */
        code {
            background-color: #f5f5f5;
            padding: 2px 4px;
            border-radius: 3px;
            font-family: 'Courier New', monospace;
        }

        pre {
            background-color: #f5f5f5;
            padding: 10px;
            border-radius: 5px;
            overflow-x: auto;
            font-family: 'Courier New', monospace;
        }

        /* 确保引用样式 */
        blockquote {
            border-left: 4px solid #ddd;
            margin: 20px 0;
            padding: 10px 20px;
            background-color: #f9f9f9;
        }

        /* 确保分割线样式 */
        hr {
            border: none;
            border-top: 1px solid #ddd;
            margin: 20px 0;
        }
    </style>
</head>
<body>
    ${this.extractBodyContent(html)}
</body>
</html>`;

      console.log('完整的HTML文档:', fullHtml);
      return fullHtml;
    },
    extractUserStyles(html) {
      // 提取用户HTML中的<style>标签内容
      const styleMatch = html.match(/<style[^>]*>([\s\S]*?)<\/style>/i);
      if (styleMatch) {
        let styles = styleMatch[1];
        // 增强样式，添加!important确保优先级
        styles = styles.replace(/([^{}]+)\s*{\s*([^{}]+)\s*}/g, (match, selector, rules) => {
          // 为每个CSS属性添加!important
          const enhancedRules = rules.split(';').map(rule => {
            rule = rule.trim();
            if (rule && !rule.includes('!important')) {
              return rule + ' !important';
            }
            return rule;
          }).join('; ');
          return `${selector} { ${enhancedRules} }`;
        });
        console.log('增强的用户样式:', styles);
        return styles;
      }
      return '';
    },
    extractBodyContent(html) {
      // 提取body标签内的内容，如果没有body标签则返回整个HTML
      const bodyMatch = html.match(/<body[^>]*>([\s\S]*?)<\/body>/i);
      if (bodyMatch) {
        return bodyMatch[1];
      }

      // 如果没有body标签，移除head和style标签，只保留body内容
      let content = html
        .replace(/<head[^>]*>[\s\S]*?<\/head>/gi, '')
        .replace(/<style[^>]*>[\s\S]*?<\/style>/gi, '')
        .replace(/<html[^>]*>/gi, '')
        .replace(/<\/html>/gi, '');

      return content;
    },

    getSanitizedHtml(html) {
      // 获取净化后的HTML并确保正确渲染
      if (!html) return '';

      // 调试信息 - 在开发环境中可以帮助诊断问题
      console.log('Original HTML:', html);

      // 检查HTML是否被转义，如果是则先解码
      let processedHtml = html;
      if (html.includes('&lt;') || html.includes('&gt;') || html.includes('&amp;')) {
        console.log('检测到HTML被转义，进行解码');
        processedHtml = html
          .replace(/&lt;/g, '<')
          .replace(/&gt;/g, '>')
          .replace(/&amp;/g, '&')
          .replace(/&quot;/g, '"')
          .replace(/&#x27;/g, "'");
        console.log('解码后的HTML:', processedHtml);
      }

      // 先净化HTML
      const sanitized = this.sanitizeHtml(processedHtml);
      console.log('Sanitized HTML:', sanitized);

      // 返回完整的HTML文档结构，确保在iframe中正确渲染
      // 使用数组join方法避免多余的空白字符
      const result = [
        '<!DOCTYPE html>',
        '<html>',
        '<head>',
        '  <meta charset="UTF-8">',
        '  <meta name="viewport" content="width=device-width, initial-scale=1.0">',
        '  <title>Template Preview</title>',
        '  <style>',
        '    body {',
        '      font-family: -apple-system, BlinkMacSystemFont, \'Segoe UI\', Roboto, \'Helvetica Neue\', Arial, sans-serif;',
        '      margin: 0;',
        '      padding: 20px;',
        '      line-height: 1.6;',
        '      color: #333;',
        '    }',
        '    table {',
        '      border-collapse: collapse;',
        '      width: 100%;',
        '    }',
        '    th, td {',
        '      border: 1px solid #ddd;',
        '      padding: 8px;',
        '      text-align: left;',
        '    }',
        '    th {',
        '      background-color: #f2f2f2;',
        '    }',
        '  </style>',
        '</head>',
        '<body>',
        sanitized,
        '</body>',
        '</html>'
      ].join('');

      // 调试信息 - 在开发环境中可以帮助诊断问题
      console.log('Final HTML:', result);

      return result;
    },

    updateIframeContent() {
      // 现在使用srcdoc属性，iframe会自动更新，这里只需要触发重新渲染
      console.log('触发iframe重新渲染');
      this.$forceUpdate();
    },
    openInNewWindow() {
      // 在新窗口中打开HTML预览，完全绕过iframe限制
      if (!this.form[this.previewTemplateType]) {
        this.$message.warning('请先选择要预览的模板内容');
        return;
      }

      const htmlContent = this.getIframeContent();
      const newWindow = window.open('', '_blank', 'width=1200,height=800,scrollbars=yes,resizable=yes');

      if (newWindow) {
        newWindow.document.write(htmlContent);
        newWindow.document.close();
        newWindow.focus();
      } else {
        this.$message.error('无法打开新窗口，请检查浏览器设置');
      }
    },
    downloadHtml() {
      // 下载HTML文件，用户可以在浏览器中打开查看完整效果
      if (!this.form[this.previewTemplateType]) {
        this.$message.warning('请先选择要预览的模板内容');
        return;
      }

      const htmlContent = this.getIframeContent();
      const blob = new Blob([htmlContent], {type: 'text/html;charset=utf-8'});
      const link = document.createElement('a');
      const fileName = `template_${this.getTemplateTypeName(this.previewTemplateType)}_${new Date().getTime()}.html`;

      if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveOrOpenBlob(blob, fileName);
      } else {
        link.style.display = 'none';
        link.href = window.URL.createObjectURL(blob);
        link.setAttribute('download', fileName);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(link.href);
      }

      this.$message.success('HTML文件下载成功，可以在浏览器中打开查看完整效果');
    },
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id != null) {
            updateTemplates(this.form).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.$emit("success");
              this.handleClose();
            });
          } else {
            addTemplates(this.form).then(response => {
              this.$modal.msgSuccess("新增成功");
              this.$emit("success");
              this.handleClose();
            });
          }
        }
      });
    },
    downloadDocument() {
      downloadDocument().then(response => {
        const blob = new Blob([response], {type: 'application/octet-stream'});
        const link = document.createElement('a');
        const fileName = '使用文档.docx';
        if (window.navigator.msSaveOrOpenBlob) {
          window.navigator.msSaveOrOpenBlob(blob, fileName);
        } else {
          link.style.display = 'none';
          link.href = window.URL.createObjectURL(blob);
          link.setAttribute('download', fileName);
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
      });
    }
  }
};
</script>

<style scoped>
.preview-container {
  min-height: 300px;
}

.template-info {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.template-info h4 {
  margin: 5px 0;
  color: #333;
}

.security-level-control {
  margin-top: 10px;
  padding: 10px 0;
  border-top: 1px solid #eee;
}

.security-level-control span {
  margin-right: 10px;
  font-weight: bold;
  color: #666;
}

.preview-warning {
  margin: 10px 0;
  padding: 8px 12px;
  background-color: #fef0f0;
  border: 1px solid #fbc4c4;
  border-radius: 4px;
  color: #f56c6c;
  font-size: 14px;
  display: flex;
  align-items: center;
}

.preview-warning i {
  margin-right: 8px;
  font-size: 16px;
}

.preview-content {
  min-height: 400px;
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background-color: #f9f9f9;
  margin-bottom: 20px;
  position: relative;
}

.template-preview-frame {
  width: 100%;
  height: 400px;
  border: none;
  background-color: white;
}

.template-preview-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #999;
  font-size: 16px;
}

.template-selector {
  text-align: center;
  padding: 10px 0;
}

.dialog-footer {
  text-align: right;
}
</style>
