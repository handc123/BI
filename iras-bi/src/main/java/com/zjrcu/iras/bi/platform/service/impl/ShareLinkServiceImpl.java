package com.zjrcu.iras.bi.platform.service.impl;

import com.zjrcu.iras.bi.platform.domain.ShareLink;
import com.zjrcu.iras.bi.platform.service.IShareLinkService;
import com.zjrcu.iras.bi.platform.service.ShareLinkAccessResult;
import com.zjrcu.iras.common.exception.ServiceException;
import com.zjrcu.iras.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 共享链接服务实现
 * 注意：这是简化实现，实际应该使用数据库存储
 *
 * @author iras
 */
@Service
public class ShareLinkServiceImpl implements IShareLinkService {

    private static final Logger log = LoggerFactory.getLogger(ShareLinkServiceImpl.class);

    /**
     * 共享链接存储（简化实现，实际应使用数据库）
     */
    private final Map<String, ShareLink> shareLinkStore = new ConcurrentHashMap<>();

    @Override
    public ShareLink generateShareLink(String resourceType, Long resourceId, String password, 
                                      int expireDays, int accessLimit) {
        if (StringUtils.isEmpty(resourceType) || resourceId == null) {
            throw new ServiceException("资源类型和资源ID不能为空");
        }

        // 生成唯一共享码
        String shareCode = generateShareCode();

        // 创建共享链接
        ShareLink shareLink = new ShareLink();
        shareLink.setShareCode(shareCode);
        shareLink.setResourceType(resourceType);
        shareLink.setResourceId(resourceId);
        shareLink.setPassword(password);
        shareLink.setAccessLimit(accessLimit);
        shareLink.setAccessCount(0);
        shareLink.setStatus("0");
        shareLink.setCreateTime(new Date());

        // 设置过期时间
        if (expireDays > 0) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, expireDays);
            shareLink.setExpireTime(calendar.getTime());
        }

        // 存储共享链接
        shareLinkStore.put(shareCode, shareLink);

        log.info("生成共享链接: shareCode={}, resourceType={}, resourceId={}", 
                shareCode, resourceType, resourceId);

        return shareLink;
    }

    @Override
    public ShareLink getShareLinkByCode(String shareCode) {
        if (StringUtils.isEmpty(shareCode)) {
            return null;
        }
        return shareLinkStore.get(shareCode);
    }

    @Override
    public ShareLinkAccessResult validateAccess(String shareCode, String password) {
        if (StringUtils.isEmpty(shareCode)) {
            return ShareLinkAccessResult.denied("共享码不能为空");
        }

        // 获取共享链接
        ShareLink shareLink = shareLinkStore.get(shareCode);
        if (shareLink == null) {
            return ShareLinkAccessResult.denied("共享链接不存在");
        }

        // 检查状态
        if (!"0".equals(shareLink.getStatus())) {
            return ShareLinkAccessResult.denied("共享链接已被撤销");
        }

        // 检查过期时间
        if (shareLink.isExpired()) {
            return ShareLinkAccessResult.denied("共享链接已过期");
        }

        // 检查访问次数限制
        if (shareLink.isAccessLimitReached()) {
            return ShareLinkAccessResult.denied("共享链接访问次数已达上限");
        }

        // 检查密码
        if (StringUtils.isNotEmpty(shareLink.getPassword())) {
            if (StringUtils.isEmpty(password)) {
                return ShareLinkAccessResult.denied("需要输入访问密码");
            }
            if (!shareLink.getPassword().equals(password)) {
                return ShareLinkAccessResult.denied("访问密码错误");
            }
        }

        // 验证通过
        return ShareLinkAccessResult.allowed(shareLink.getResourceType(), shareLink.getResourceId());
    }

    @Override
    public void recordAccess(String shareCode) {
        if (StringUtils.isEmpty(shareCode)) {
            return;
        }

        ShareLink shareLink = shareLinkStore.get(shareCode);
        if (shareLink != null) {
            shareLink.setAccessCount(shareLink.getAccessCount() + 1);
            log.debug("记录共享链接访问: shareCode={}, accessCount={}", 
                     shareCode, shareLink.getAccessCount());
        }
    }

    @Override
    public int revokeShareLink(String shareCode) {
        if (StringUtils.isEmpty(shareCode)) {
            return 0;
        }

        ShareLink shareLink = shareLinkStore.get(shareCode);
        if (shareLink != null) {
            shareLink.setStatus("1");
            log.info("撤销共享链接: shareCode={}", shareCode);
            return 1;
        }

        return 0;
    }

    @Override
    public int deleteExpiredLinks() {
        int count = 0;
        Date now = new Date();

        for (Map.Entry<String, ShareLink> entry : shareLinkStore.entrySet()) {
            ShareLink shareLink = entry.getValue();
            if (shareLink.getExpireTime() != null && shareLink.getExpireTime().before(now)) {
                shareLinkStore.remove(entry.getKey());
                count++;
            }
        }

        if (count > 0) {
            log.info("删除过期共享链接: count={}", count);
        }

        return count;
    }

    /**
     * 生成共享码
     *
     * @return 共享码
     */
    private String generateShareCode() {
        // 生成8位随机码
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
