package cc.endmc.server.mapper.permission;

import cc.endmc.server.domain.permission.BanlistInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 封禁管理Mapper接口
 *
 * @author ruoyi
 * @date 2024-03-28
 */
@Mapper
public interface BanlistInfoMapper {
    /**
     * 查询封禁管理
     *
     * @param id 封禁管理主键
     * @return 封禁管理
     */
    public BanlistInfo selectBanlistInfoById(Long id);

    /**
     * 查询封禁管理列表
     *
     * @param banlistInfo 封禁管理
     * @return 封禁管理集合
     */
    public List<BanlistInfo> selectBanlistInfoList(BanlistInfo banlistInfo);

    /**
     * 新增封禁管理
     *
     * @param banlistInfo 封禁管理
     * @return 结果
     */
    public int insertBanlistInfo(BanlistInfo banlistInfo);

    /**
     * 修改封禁管理
     *
     * @param banlistInfo 封禁管理
     * @return 结果
     */
    public int updateBanlistInfo(BanlistInfo banlistInfo);

    /**
     * 删除封禁管理
     *
     * @param id 封禁管理主键
     * @return 结果
     */
    public int deleteBanlistInfoById(Long id);

    /**
     * 批量删除封禁管理
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBanlistInfoByIds(Long[] ids);
}
