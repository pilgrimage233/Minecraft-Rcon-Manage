package com.ruoyi.server.controller.other;

import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.domain.other.IpLimitInfo;
import com.ruoyi.server.service.other.IIpLimitInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * IP限流Controller
 *
 * @author ruoyi
 * @date 2024-03-22
 */
@RestController
@RequestMapping("/ipinfo/limit")
public class IpLimitInfoController extends BaseController {
    @Autowired
    private IIpLimitInfoService ipLimitInfoService;

    /**
     * 查询IP限流列表
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:list')")
    @GetMapping("/list")
    public TableDataInfo list(IpLimitInfo ipLimitInfo) {
        startPage();
        List<IpLimitInfo> list = ipLimitInfoService.selectIpLimitInfoList(ipLimitInfo);
        return getDataTable(list);
    }

    /**
     * 导出IP限流列表
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:export')")
    @Log(title = "IP限流", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, IpLimitInfo ipLimitInfo) {
        List<IpLimitInfo> list = ipLimitInfoService.selectIpLimitInfoList(ipLimitInfo);
        ExcelUtil<IpLimitInfo> util = new ExcelUtil<IpLimitInfo>(IpLimitInfo.class);
        util.exportExcel(response, list, "IP限流数据");
    }

    /**
     * 获取IP限流详细信息
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(ipLimitInfoService.selectIpLimitInfoById(id));
    }

    /**
     * 新增IP限流
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:add')")
    @Log(title = "IP限流", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody IpLimitInfo ipLimitInfo) {
        return toAjax(ipLimitInfoService.insertIpLimitInfo(ipLimitInfo));
    }

    /**
     * 修改IP限流
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:edit')")
    @Log(title = "IP限流", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody IpLimitInfo ipLimitInfo) {
        return toAjax(ipLimitInfoService.updateIpLimitInfo(ipLimitInfo));
    }

    /**
     * 删除IP限流
     */
    @PreAuthorize("@ss.hasPermi('ipinfo:limit:remove')")
    @Log(title = "IP限流", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(ipLimitInfoService.deleteIpLimitInfoByIds(ids));
    }
}
