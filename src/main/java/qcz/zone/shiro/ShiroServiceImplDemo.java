package qcz.zone.shiro;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import qcz.zone.shiro.entity.AbstractUrlAccessStrategy;
import qcz.zone.shiro.entity.AbstractUser;
import qcz.zone.shiro.entity.AbstractUserAuths;
import qcz.zone.shiro.service.ShiroService;

import javax.annotation.Resource;
import java.util.ArrayList;
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
    public AbstractUser getAbstractUser(Object principal, String password) {
//        return shiroDAO.getAbstractUser(principal, password);
        return null;
    }

    @Override
    public AbstractUserAuths getUserAuths(Object principal) {
//        return shiroDAO.getUserAuths(principal);
        return null;
    }

    @Override
    public List<AbstractUrlAccessStrategy> getAllUrlAccessStrategy() {
//        List<UrlAccessStrategy> lstUrlAccessStrategy = shiroDAO.getAllUrlAccessStrategy();
//        if (CollectionUtils.isEmpty(lstUrlAccessStrategy))
//            return null;
//
//        return  new ArrayList<AbstractUrlAccessStrategy>(lstUrlAccessStrategy);
        return null;
    }
}
