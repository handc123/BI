package com.zjrcu.iras.bi.platform.service;

import com.zjrcu.iras.bi.platform.domain.ShareLink;

/**
 * 共享链接服务接口
 *
 * @author iras
 */
public interface IShareLinkService {

    /**
     * 生成共享链接
     *
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     * @param password 访问密码（可选）
     * @param expireDays 过期天数（0表示永不过期）
     * @param accessLimit 访问次数限制（0表示无限制）
     * @return 共享链接
     */
    ShareLink generateShareLink(String resourceType, Long resourceId, String password, 
                               int expireDays, int accessLimit);

    /**
     * 根据共享码获取共享链接
     *
     * @param shareCode 共享码
     * @return 共享链接
     */
    ShareLink getShareLinkByCode(String shareCode);

    /**
     * 验证共享链接访问权限
     *
     * @param shareCode 共享码
     * @param password 访问密码（可选）
     * @return 验证结果
     */
    ShareLinkAccessResult validateAccess(String shareCode, String password);

    /**
     * 记录访问
     *
     * @param shareCode 共享码
     */
    void recordAccess(String shareCode);

    /**
     * 撤销共享链接
     *
     * @param shareCode 共享码
     * @return 结果
     */
    int revokeShareLink(String shareCode);

    /**
     * 删除过期的共享链接
     *
     * @return 删除数量
     */
    int deleteExpiredLinks();
}
