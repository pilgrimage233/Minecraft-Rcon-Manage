package com.ruoyi.server.controller.player;

import com.ruoyi.common.annotation.AddOrUpdateFilter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.server.domain.player.PlayerDetails;
import com.ruoyi.server.service.player.IPlayerDetailsService;
import com.ruoyi.server.service.player.impl.PlayerDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 玩家详情Controller
 *
 * @author Memory
 * @date 2024-12-31
 */
@RestController
@RequestMapping("/player/details")
public class PlayerDetailsController extends BaseController {
    @Autowired
    private IPlayerDetailsService playerDetailsService;

    /**
     * 查询玩家详情列表
     */
    @PreAuthorize("@ss.hasPermi('player:details:list')")
    @GetMapping("/list")
    public TableDataInfo list(PlayerDetails playerDetails) {
        startPage();
        List<PlayerDetails> list = playerDetailsService.selectPlayerDetailsList(playerDetails);
        return getDataTable(list);
    }

    /**
     * 导出玩家详情列表
     */
    @PreAuthorize("@ss.hasPermi('player:details:export')")
    @Log(title = "玩家详情", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PlayerDetails playerDetails) {
        List<PlayerDetails> list = playerDetailsService.selectPlayerDetailsList(playerDetails);
        ExcelUtil<PlayerDetails> util = new ExcelUtil<PlayerDetails>(PlayerDetails.class);
        util.exportExcel(response, list, "玩家详情数据");
    }

    /**
     * 获取玩家详情详细信息
     */
    @PreAuthorize("@ss.hasPermi('player:details:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(playerDetailsService.selectPlayerDetailsById(id));
    }

    /**
     * 新增玩家详情
     */
    @PreAuthorize("@ss.hasPermi('player:details:add')")
    @Log(title = "玩家详情", businessType = BusinessType.INSERT)
    @AddOrUpdateFilter(add = true)
    @PostMapping
    public AjaxResult add(@RequestBody PlayerDetails playerDetails) {
        return toAjax(playerDetailsService.insertPlayerDetails(playerDetails));
    }

    /**
     * 修改玩家详情
     */
    @PreAuthorize("@ss.hasPermi('player:details:edit')")
    @Log(title = "玩家详情", businessType = BusinessType.UPDATE)
    @AddOrUpdateFilter(edit = true)
    @PutMapping
    public AjaxResult edit(@RequestBody PlayerDetails playerDetails) {
        return toAjax(((PlayerDetailsServiceImpl) playerDetailsService).updatePlayerDetails(playerDetails, true));
    }

    /**
     * 删除玩家详情
     */
    @PreAuthorize("@ss.hasPermi('player:details:remove')")
    @Log(title = "玩家详情", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(playerDetailsService.deletePlayerDetailsByIds(ids));
    }
}
