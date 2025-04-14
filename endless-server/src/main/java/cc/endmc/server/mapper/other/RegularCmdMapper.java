package cc.endmc.server.mapper.other;

import cc.endmc.server.domain.other.RegularCmd;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 定时命令Mapper接口
 *
 * @author ruoyi
 * @date 2025-02-14
 */
@Mapper
public interface RegularCmdMapper {
    /**
     * 查询定时命令
     *
     * @param id 定时命令主键
     * @return 定时命令
     */
    public RegularCmd selectRegularCmdById(Long id);

    /**
     * 查询定时命令列表
     *
     * @param regularCmd 定时命令
     * @return 定时命令集合
     */
    public List<RegularCmd> selectRegularCmdList(RegularCmd regularCmd);

    /**
     * 新增定时命令
     *
     * @param regularCmd 定时命令
     * @return 结果
     */
    public int insertRegularCmd(RegularCmd regularCmd);

    /**
     * 修改定时命令
     *
     * @param regularCmd 定时命令
     * @return 结果
     */
    public int updateRegularCmd(RegularCmd regularCmd);

    /**
     * 删除定时命令
     *
     * @param id 定时命令主键
     * @return 结果
     */
    public int deleteRegularCmdById(Long id);

    /**
     * 批量删除定时命令
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteRegularCmdByIds(Long[] ids);
}
