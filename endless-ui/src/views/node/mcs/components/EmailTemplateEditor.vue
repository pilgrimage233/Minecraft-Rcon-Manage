<template>
  <div>
    <el-button size="small" type="primary" icon="el-icon-plus" @click="handleAdd">新增模板</el-button>
    <el-table :data="templates" size="small" style="margin-top: 10px">
      <el-table-column label="模板标识" prop="templateKey" width="150" />
      <el-table-column label="模板名称" prop="templateName" width="150" />
      <el-table-column label="主题" prop="subject" show-overflow-tooltip />
      <el-table-column label="操作" width="150" align="center">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="mini" type="text" style="color:#F56C6C" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑对话框 -->
    <el-dialog :visible.sync="editOpen" :title="editTitle" width="700px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px" size="small">
        <el-form-item label="模板标识" prop="templateKey">
          <el-input v-model="editForm.templateKey" :disabled="!!editForm.id" placeholder="如: crash_notify" />
        </el-form-item>
        <el-form-item label="模板名称" prop="templateName">
          <el-input v-model="editForm.templateName" placeholder="如: 崩溃通知" />
        </el-form-item>
        <el-form-item label="邮件主题" prop="subject">
          <el-input v-model="editForm.subject" placeholder="支持 {变量名} 占位符" />
        </el-form-item>
        <el-form-item label="邮件内容" prop="content">
          <el-input v-model="editForm.content" :autosize="{ minRows: 8, maxRows: 20 }" type="textarea" placeholder="HTML 模板内容，支持 {变量名} 占位符" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="editForm.description" placeholder="模板用途说明" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button size="small" @click="editOpen = false">取 消</el-button>
        <el-button size="small" type="primary" @click="submitEditForm">保 存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listEmailTemplate, addEmailTemplate, updateEmailTemplate, delEmailTemplate } from '@/api/node/settings'

export default {
  name: 'EmailTemplateEditor',
  data() {
    return {
      templates: [],
      editOpen: false,
      editTitle: '',
      editForm: { id: null, templateKey: '', templateName: '', subject: '', content: '', description: '' },
      editRules: {
        templateKey: [{ required: true, message: '请输入模板标识', trigger: 'blur' }],
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        subject: [{ required: true, message: '请输入邮件主题', trigger: 'blur' }],
        content: [{ required: true, message: '请输入邮件内容', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadTemplates()
  },
  methods: {
    async loadTemplates() {
      try {
        const res = await listEmailTemplate({ pageSize: 100 })
        this.templates = res.rows || []
      } catch (e) {
        console.warn('加载模板失败:', e)
      }
    },
    handleAdd() {
      this.editTitle = '新增邮件模板'
      this.editForm = { id: null, templateKey: '', templateName: '', subject: '', content: '', description: '' }
      this.editOpen = true
    },
    handleEdit(row) {
      this.editTitle = '编辑邮件模板'
      this.editForm = { ...row }
      this.editOpen = true
    },
    handleDelete(row) {
      this.$confirm('确认删除模板 "' + row.templateName + '"?', '提示', { type: 'warning' }).then(() => {
        return delEmailTemplate(row.id)
      }).then(() => {
        this.$message.success('删除成功')
        this.loadTemplates()
      })
    },
    submitEditForm() {
      this.$refs.editForm.validate(valid => {
        if (!valid) return
        const action = this.editForm.id ? updateEmailTemplate : addEmailTemplate
        action(this.editForm).then(res => {
          if (res.code === 200) {
            this.$message.success('保存成功')
            this.editOpen = false
            this.loadTemplates()
          }
        })
      })
    }
  }
}
</script>
