package qcz.zone.shiro;

import org.springframework.stereotype.Service;
import qcz.zone.shiro.entity.ShiroUrlAccessStrategy;
import qcz.zone.shiro.entity.ShiroUser;
import qcz.zone.shiro.entity.ShiroUserAuths;
import qcz.zone.shiro.service.ShiroService;

import java.util.List;

/**
 * @author: qiuchengze
 * @email: eric.qjc@163.com
 * @web: http://www.fast-im.com/
 * @create: 2020 - 01 - 29
 */

@Service
public class ShiroServiceImplDemo implements ShiroService {

//    @Resource
//    private ShiroDAO shiroDAO;


    @Override
    public ShiroUser getAbstractUser(Object principal, String password) {
//        return shiroDAO.getAbstractUser(principal, password);
        return null;
    }

    @Override
    public ShiroUserAuths getUserAuths(Object principal) {
//        return shiroDAO.getUserAuths(principal);
        return null;
    }

    @Override
    public List<ShiroUrlAccessStrategy> getAllUrlAccessStrategy() {
//        List<UrlAccessStrategy> lstUrlAccessStrategy = shiroDAO.getAllUrlAccessStrategy();
//        if (CollectionUtils.isEmpty(lstUrlAccessStrategy))
//            return null;
//
//        return  lstUrlAccessStrategy;
        return null;
    }
}
