package cc.endmc.server.controller.permission;

import cc.endmc.common.annotation.AddOrUpdateFilter;
import cc.endmc.common.annotation.Log;
import cc.endmc.common.core.controller.BaseController;
import cc.endmc.common.core.domain.AjaxResult;
import cc.endmc.common.core.page.TableDataInfo;
import cc.endmc.common.enums.BusinessType;
import cc.endmc.common.utils.poi.ExcelUtil;
import cc.endmc.server.domain.permission.BanlistInfo;
import cc.endmc.server.service.permission.IBanlistInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 封禁管理Controller
 *
 * @author ruoyi
 * @date 2024-03-28
 */
@RestController
@RequestMapping("/mc/banlist")
public class BanlistInfoController extends BaseController {
    @Autowired
    private IBanlistInfoService banlistInfoService;

    /**
     * 查询封禁管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:list')")
    @GetMapping("/list")
    public TableDataInfo list(BanlistInfo banlistInfo) {
        startPage();
        List<BanlistInfo> list = banlistInfoService.selectBanlistInfoList(banlistInfo);
        return getDataTable(list);
    }

    /**
     * 导出封禁管理列表
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:export')")
    @Log(title = "封禁管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BanlistInfo banlistInfo) {
        List<BanlistInfo> list = banlistInfoService.selectBanlistInfoList(banlistInfo);
        ExcelUtil<BanlistInfo> util = new ExcelUtil<BanlistInfo>(BanlistInfo.class);
        util.exportExcel(response, list, "封禁管理数据");
    }

    /**
     * 获取封禁管理详细信息
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(banlistInfoService.selectBanlistInfoById(id));
    }

    /**
     * 新增封禁管理
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:add')")
    @Log(title = "封禁管理", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody BanlistInfo banlistInfo) {
        return toAjax(banlistInfoService.insertBanlistInfo(banlistInfo));
    }

    /**
     * 修改封禁管理
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:edit')")
    @Log(title = "封禁管理", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody BanlistInfo banlistInfo) {
        return toAjax(banlistInfoService.updateBanlistInfo(banlistInfo, true));
    }

    /**
     * 删除封禁管理
     */
    @PreAuthorize("@ss.hasPermi('mc:banlist:remove')")
    @Log(title = "封禁管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(banlistInfoService.deleteBanlistInfoByIds(ids));
    }
}
