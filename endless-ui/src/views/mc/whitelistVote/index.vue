<template>
  <div class="app-container">
    <el-form :inline="true" :model="voteQueryParams" label-width="68px" size="small">
      <el-form-item label="状态" prop="status">
        <el-select v-model="voteQueryParams.status" clearable placeholder="请选择状态" style="width: 160px;">
          <el-option label="进行中" value="ONGOING"/>
          <el-option label="已通过" value="PASSED"/>
          <el-option label="已拒绝" value="REJECTED"/>
          <el-option label="已过期" value="EXPIRED"/>
        </el-select>
      </el-form-item>
      <el-form-item label="目标玩家" prop="targetPlayerName">
        <el-input
          v-model="voteQueryParams.targetPlayerName"
          clearable
          placeholder="请输入玩家名"
          @keyup.enter.native="handleVoteQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button icon="el-icon-search" size="mini" type="primary" @click="handleVoteQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetVoteQuery">重置</el-button>
        <el-button
          v-hasPermi="['mc:whitelist:vote:create']"
          icon="el-icon-plus"
          size="mini"
          type="success"
          @click="handleCreateVote()"
        >发起投票
        </el-button>
        <el-button
          v-hasPermi="['mc:whitelist:vote:template:add']"
          icon="el-icon-setting"
          size="mini"
          type="warning"
          @click="handleCreateVoteTemplate"
        >新增自定义投票
        </el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="voteLoading" :data="voteList">
      <el-table-column align="center" label="投票ID" prop="id" width="90"/>
      <el-table-column align="center" label="投票类型" min-width="140" prop="templateName" show-overflow-tooltip/>
      <el-table-column align="center" label="目标玩家" min-width="140" prop="targetPlayerName" show-overflow-tooltip/>
      <el-table-column align="center" label="同意/需票" min-width="100">
        <template slot-scope="scope">
          {{ scope.row.agreeVotes }}/{{ scope.row.requiredVotes }}
        </template>
      </el-table-column>
      <el-table-column align="center" label="反对票" prop="rejectVotes" width="90"/>
      <el-table-column align="center" label="状态" prop="status" width="110">
        <template slot-scope="scope">
          <el-tag :type="getVoteStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="发起人" prop="initiatorUserName" width="120"/>
      <el-table-column align="center" label="到期时间" prop="expireTime" width="170">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.expireTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" class-name="small-padding fixed-width" label="操作" width="200">
        <template slot-scope="scope">
          <el-button
            icon="el-icon-view"
            size="mini"
            type="text"
            @click="handleVoteDetail(scope.row)"
          >详情
          </el-button>
          <el-button
            v-if="scope.row.status === 'ONGOING'"
            v-hasPermi="['mc:whitelist:vote:cast']"
            icon="el-icon-thumb"
            size="mini"
            type="text"
            @click="handleCastVote(scope.row)"
          >跟投
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="voteTotal>0"
      :limit.sync="voteQueryParams.pageSize"
      :page.sync="voteQueryParams.pageNum"
      :total="voteTotal"
      @pagination="getVoteList"
    />

    <el-dialog :visible.sync="voteOpen" append-to-body title="发起投票" width="560px">
      <el-form ref="voteForm" :model="voteForm" :rules="voteRules" label-width="100px" size="medium">
        <el-form-item label="投票类型" prop="templateId">
          <el-select v-model="voteForm.templateId" clearable placeholder="请选择投票类型" style="width: 100%;">
            <el-option
              v-for="item in voteTemplateOptions"
              :key="item.id"
              :label="item.templateName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="目标玩家" prop="targetPlayerName">
          <el-input v-model="voteForm.targetPlayerName" clearable placeholder="请输入目标玩家"/>
        </el-form-item>
        <el-form-item label="白名单ID" prop="targetWhitelistId">
          <el-input-number v-model="voteForm.targetWhitelistId" :min="1" controls-position="right"
                           style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="发起原因" prop="reason">
          <el-input v-model="voteForm.reason" :rows="3" maxlength="500" placeholder="请输入投票原因" show-word-limit
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCreateVote">确 定</el-button>
        <el-button @click="voteOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="voteCastOpen" append-to-body title="参与投票" width="520px">
      <el-form ref="voteCastForm" :model="voteCastForm" :rules="voteCastRules" label-width="100px" size="medium">
        <el-form-item label="投票结果" prop="voteDecision">
          <el-radio-group v-model="voteCastForm.voteDecision">
            <el-radio :label="1">同意</el-radio>
            <el-radio :label="2">反对</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="voteComment">
          <el-input v-model="voteCastForm.voteComment" :rows="3" maxlength="255" placeholder="请输入备注（可选）"
                    show-word-limit type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCastVote">确 定</el-button>
        <el-button @click="voteCastOpen = false">取 消</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="voteDetailOpen" append-to-body title="投票详情" width="760px">
      <div v-if="voteDetail">
        <el-descriptions :column="2" border size="small" title="投票信息">
          <el-descriptions-item label="投票类型">{{ voteDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="投票状态">{{ voteDetail.status }}</el-descriptions-item>
          <el-descriptions-item label="目标玩家">{{ voteDetail.targetPlayerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="白名单ID">{{ voteDetail.targetWhitelistId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="票数">{{ voteDetail.agreeVotes }}/{{
              voteDetail.requiredVotes
            }}（反对{{ voteDetail.rejectVotes }}）
          </el-descriptions-item>
          <el-descriptions-item label="发起人">{{ voteDetail.initiatorUserName }}</el-descriptions-item>
          <el-descriptions-item label="到期时间">{{
              parseTime(voteDetail.expireTime, '{y}-{m}-{d} {h}:{i}:{s}')
            }}
          </el-descriptions-item>
          <el-descriptions-item label="发起原因">{{ voteDetail.reason || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">投票记录</el-divider>
        <el-table :data="voteDetail.voteRecords || []" size="small">
          <el-table-column align="center" label="用户" prop="voterUserName"/>
          <el-table-column align="center" label="结果" prop="voteDecision" width="90">
            <template slot-scope="scope">
              {{ scope.row.voteDecision === 1 ? '同意' : '反对' }}
            </template>
          </el-table-column>
          <el-table-column align="center" label="备注" prop="voteComment" show-overflow-tooltip/>
          <el-table-column align="center" label="时间" prop="createTime" width="180">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime, '{y}-{m}-{d} {h}:{i}:{s}') }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="voteDetailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>

    <el-dialog :visible.sync="voteTemplateOpen" append-to-body title="新增自定义投票" width="620px">
      <el-form ref="voteTemplateForm" :model="voteTemplateForm" :rules="voteTemplateRules" label-width="120px"
               size="small">
        <el-form-item label="投票名称" prop="templateName">
          <el-input v-model="voteTemplateForm.templateName" maxlength="128" placeholder="例如：更改天气"/>
        </el-form-item>
        <el-form-item label="执行命令" prop="actionCommandTemplate">
          <el-input v-model="voteTemplateForm.actionCommandTemplate" maxlength="500" placeholder="例如：time set day"/>
        </el-form-item>
        <el-form-item label="目标类型" prop="targetType">
          <el-select v-model="voteTemplateForm.targetType" style="width: 100%;">
            <el-option label="无目标(OTHER)" value="OTHER"/>
            <el-option label="玩家(PLAYER)" value="PLAYER"/>
            <el-option label="白名单(WHITELIST)" value="WHITELIST"/>
          </el-select>
        </el-form-item>
        <el-form-item label="通过票数" prop="minRequiredVotes">
          <el-input-number v-model="voteTemplateForm.minRequiredVotes" :max="999" :min="1" controls-position="right"
                           style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="投票时长(秒)" prop="voteDurationSeconds">
          <el-input-number v-model="voteTemplateForm.voteDurationSeconds" :max="86400" :min="30"
                           controls-position="right" style="width: 100%;"/>
        </el-form-item>
        <el-form-item label="必须填写原因">
          <el-switch v-model="voteTemplateNeedReasonBool" active-text="是" inactive-text="否"/>
        </el-form-item>
        <el-form-item label="模板描述" prop="templateDesc">
          <el-input v-model="voteTemplateForm.templateDesc" :rows="3" maxlength="500" placeholder="可选" show-word-limit
                    type="textarea"/>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitCreateVoteTemplate">确 定</el-button>
        <el-button @click="voteTemplateOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  castWhitelistVote,
  createCustomWhitelistVoteTemplate,
  createWhitelistVote,
  getWhitelistVoteDetail,
  listWhitelistVotes,
  listWhitelistVoteTemplates
} from "@/api/mc/whitelist";
import {parseTime} from "@/utils/ruoyi";

export default {
  name: "WhitelistVote",
  data() {
    return {
      voteLoading: false,
      voteTotal: 0,
      voteList: [],
      voteTemplateOptions: [],
      voteOpen: false,
      voteCastOpen: false,
      voteDetailOpen: false,
      voteTemplateOpen: false,
      voteDetail: null,
      voteQueryParams: {
        pageNum: 1,
        pageSize: 10,
        status: 'ONGOING',
        targetPlayerName: null
      },
      voteForm: {
        templateId: null,
        targetPlayerName: null,
        targetWhitelistId: null,
        reason: null
      },
      voteCastForm: {
        voteId: null,
        voteDecision: 1,
        voteComment: null
      },
      voteRules: {
        templateId: [{required: true, message: '请选择投票类型', trigger: 'change'}]
      },
      voteCastRules: {
        voteDecision: [{required: true, message: '请选择投票结果', trigger: 'change'}]
      },
      voteTemplateNeedReasonBool: false,
      voteTemplateForm: {
        templateName: null,
        templateDesc: null,
        actionCommandTemplate: null,
        minRequiredVotes: 3,
        voteDurationSeconds: 300,
        needReason: 0,
        targetType: 'OTHER'
      },
      voteTemplateRules: {
        templateName: [{required: true, message: '请输入投票名称', trigger: 'blur'}],
        actionCommandTemplate: [{required: true, message: '请输入执行命令', trigger: 'blur'}]
      }
    }
  },
  created() {
    this.getVoteTemplateList();
    this.getVoteList();
  },
  methods: {
    parseTime,
    getVoteTemplateList() {
      listWhitelistVoteTemplates().then(res => {
        this.voteTemplateOptions = res.data || [];
      });
    },
    getVoteList() {
      this.voteLoading = true;
      listWhitelistVotes(this.voteQueryParams).then(res => {
        this.voteList = res.rows || [];
        this.voteTotal = res.total || 0;
      }).finally(() => {
        this.voteLoading = false;
      });
    },
    handleVoteQuery() {
      this.voteQueryParams.pageNum = 1;
      this.getVoteList();
    },
    resetVoteQuery() {
      this.voteQueryParams = {
        pageNum: 1,
        pageSize: 10,
        status: 'ONGOING',
        targetPlayerName: null
      };
      this.getVoteList();
    },
    getVoteStatusType(status) {
      if (status === 'ONGOING') return 'warning';
      if (status === 'PASSED') return 'success';
      if (status === 'REJECTED') return 'danger';
      if (status === 'EXPIRED') return 'info';
      return '';
    },
    handleCreateVote() {
      this.voteForm = {
        templateId: null,
        targetPlayerName: null,
        targetWhitelistId: null,
        reason: null
      };
      this.voteOpen = true;
    },
    handleCreateVoteTemplate() {
      this.voteTemplateForm = {
        templateName: null,
        templateDesc: null,
        actionCommandTemplate: null,
        minRequiredVotes: 3,
        voteDurationSeconds: 300,
        needReason: 0,
        targetType: 'OTHER'
      };
      this.voteTemplateNeedReasonBool = false;
      this.voteTemplateOpen = true;
    },
    submitCreateVoteTemplate() {
      this.$refs['voteTemplateForm'].validate(valid => {
        if (!valid) {
          return;
        }
        const payload = {
          ...this.voteTemplateForm,
          needReason: this.voteTemplateNeedReasonBool ? 1 : 0
        };
        createCustomWhitelistVoteTemplate(payload).then(res => {
          this.$modal.msgSuccess(res.msg || '新增成功');
          this.voteTemplateOpen = false;
          this.getVoteTemplateList();
        });
      });
    },
    submitCreateVote() {
      this.$refs['voteForm'].validate(valid => {
        if (!valid) {
          return;
        }
        const selectedTemplate = (this.voteTemplateOptions || []).find(item => item.id === this.voteForm.templateId);
        const targetType = selectedTemplate && selectedTemplate.targetType ? selectedTemplate.targetType.toUpperCase() : 'PLAYER';
        if (targetType !== 'OTHER' && !this.voteForm.targetPlayerName && !this.voteForm.targetWhitelistId) {
          this.$modal.msgWarning('该投票类型需要填写目标玩家或白名单ID');
          return;
        }
        createWhitelistVote(this.voteForm).then(res => {
          this.$modal.msgSuccess(res.msg || '发起成功');
          this.voteOpen = false;
          this.getVoteList();
        });
      });
    },
    handleCastVote(row) {
      this.voteCastForm = {
        voteId: row.id,
        voteDecision: 1,
        voteComment: null
      };
      this.voteCastOpen = true;
    },
    submitCastVote() {
      this.$refs['voteCastForm'].validate(valid => {
        if (!valid) {
          return;
        }
        castWhitelistVote(this.voteCastForm).then(res => {
          this.$modal.msgSuccess(res.msg || '投票成功');
          this.voteCastOpen = false;
          this.getVoteList();
        });
      });
    },
    handleVoteDetail(row) {
      getWhitelistVoteDetail(row.id).then(res => {
        this.voteDetail = res.data;
        this.voteDetailOpen = true;
      });
    }
  }
};
</script>
