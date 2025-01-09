import {mount} from '@vue/test-utils'
import {createTestingPinia} from '@pinia/testing'
import WhiteListApplication from '../WhiteListApplication.vue'
import {beforeEach, describe, expect, it, vi} from 'vitest'
import {ElMessage} from 'element-plus'

vi.mock('element-plus', () => ({
    ElMessage: {
        error: vi.fn(),
        success: vi.fn()
    }
}))

describe('WhiteListApplication', () => {
    let wrapper

    beforeEach(() => {
        wrapper = mount(WhiteListApplication, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn
                })]
            }
        })
    })

    it('validates form correctly', async () => {
        const form = wrapper.vm.form

        // 测试空表单提交
        await wrapper.vm.submitForm()
        expect(ElMessage.error).toHaveBeenCalledWith('请填写完整信息')

        // 测试无效QQ号
        form.userName = 'testUser'
        form.qqNum = '123'
        form.onlineFlag = '1'
        await wrapper.vm.submitForm()
        expect(ElMessage.error).toHaveBeenCalledWith('QQ号格式错误')

        // 测试有效表单提交
        form.qqNum = '12345678'
        await wrapper.vm.submitForm()
        // 验证API调用
    })

    it('refreshes server status', async () => {
        await wrapper.vm.getOnlinePlayer(true)
        expect(wrapper.vm.serverStatus.servers).toBeDefined()
    })
})