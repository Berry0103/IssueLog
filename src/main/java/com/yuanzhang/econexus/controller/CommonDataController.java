package com.yuanzhang.econexus.controller;

import com.alibaba.fastjson.JSONObject;
import com.yuanzhang.econexus.dto.UserDTO;
import com.yuanzhang.econexus.model.Department;
import com.yuanzhang.econexus.model.Dictionary;
import com.yuanzhang.econexus.model.Project;
import com.yuanzhang.econexus.model.User;
import com.yuanzhang.econexus.repository.UserRepository;
import com.yuanzhang.econexus.service.CommonDataService;
import com.yuanzhang.econexus.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@RestController
@RequestMapping("/api/common")
public class CommonDataController {

    @Autowired
    private CommonDataService commonDataService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/departments")
    public ResponseEntity<List<Department>> getAllDepartments() {
        return ResponseEntity.ok(commonDataService.getAllDepartments());
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(commonDataService.getAllProjects());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(commonDataService.getAllUsers());
    }

    @GetMapping("/dicts/{typeName}")
    public ResponseEntity<List<Dictionary>> getDictionariesByType(@PathVariable String typeName) {
        return ResponseEntity.ok(commonDataService.getDictionariesByTypeName(typeName));
    }

    // 替换为你的企业微信配置
    private static final String CORP_ID = "wwe04a8410280bf801";
    private static final String CORP_SECRET = "hdu62HzEQb-82Z2agwQviohGm-xH82aPSpBxk1NzfD4";

    // 缓存 access_token 和过期时间
    private static String ACCESS_TOKEN = null;
    private static long EXPIRE_TIME = 0L;
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 获取企业微信 access_token（带缓存，线程安全）
     * 无需修改，直接复用，内部自动调用 OkHttp 版本的 HttpClientUtil
     */
    private String getWxWorkAccessToken() throws Exception {
        long currentTime = System.currentTimeMillis();
        // 缓存有效时直接返回
        if (ACCESS_TOKEN != null && currentTime < EXPIRE_TIME) {
            return ACCESS_TOKEN;
        }

        LOCK.lock();
        try {
            // 双重检查
            if (ACCESS_TOKEN != null && currentTime < EXPIRE_TIME) {
                return ACCESS_TOKEN;
            }

            // 构造 access_token 请求地址
            String tokenUrl = String.format(
                    "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=%s&corpsecret=%s",
                    CORP_ID, CORP_SECRET
            );

            // 调用 OkHttp 版本的 sendGet 方法（无需修改代码）
            String responseStr = HttpClientUtil.sendGet(tokenUrl);
            JSONObject jsonObj = JSONObject.parseObject(responseStr);
            int errCode = jsonObj.getInteger("errcode");
            if (errCode != 0) {
                throw new Exception("获取 access_token 失败：" + jsonObj.getString("errmsg"));
            }

            // 更新缓存
            ACCESS_TOKEN = jsonObj.getString("access_token");
            EXPIRE_TIME = currentTime + (jsonObj.getLong("expires_in") - 10) * 1000;
            return ACCESS_TOKEN;
        } finally {
            LOCK.unlock();
        }
    }


    // 后端接口
    @GetMapping("/getLaunchCode")
    @ResponseBody
    public Map<String, Object> getLaunchCode(@RequestParam String userid) {
        Map<String, Object> result = new HashMap<>();
        try {
            String thisId = "";
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // 检查用户是否已认证
            if (authentication == null || !authentication.isAuthenticated()) {
                // 如果未认证，返回401 Unauthorized
                result.put("code", 401);
                result.put("msg", "用户异常");
                return result;
            }

            // 3. 获取用户名 (authentication.getPrincipal() 通常是用户名String)
            String userName = authentication.getName();

            // 4. 使用自定义方法查询用户完整信息（包含角色和部门）
            Optional<User> userOpt = userRepository.findByUserNameWithDetails(userName);

            // 5. 处理查询结果
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                thisId = user.getQywxId();
            } else {
                // 如果用户不存在（这种情况通常不应该发生），返回404 Not Found
                result.put("code", 404);
                result.put("msg", "用户异常");
                return result;
            }

            // 1. 先获取企业微信access_token（需提前配置CorpID和Secret）
            String accessToken = getWxWorkAccessToken();
            // 2. 调用企业微信通讯录接口：根据邮箱查询用户
            String wxApiUrl = "https://qyapi.weixin.qq.com/cgi-bin/get_launch_code?access_token=" + accessToken;
            String finalThisId = thisId;
            JSONObject singleChatObj = new JSONObject();
            singleChatObj.put("userid", userid); // 内层字段：userid=lisi
            String userJson = JSONObject.toJSONString(new HashMap<String, Object>() {{
                put("operator_userid", finalThisId);
                put("single_chat", singleChatObj);
            }});
            // 3. 发送HTTP请求并解析返回结果
            String response = HttpClientUtil.sendPost(wxApiUrl,userJson);
            JSONObject jsonObj = JSONObject.parseObject(response);
            if (0 == jsonObj.getInteger("errcode")) {
                String launch_code = jsonObj.getString("launch_code"); // 获取用户唯一ID

                result.put("code", 200);
                result.put("launch_code", launch_code);
            } else {
                result.put("code", 500);
                result.put("msg", "查询企业微信用户失败：" + jsonObj.getString("errmsg"));
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "服务器异常：" + e.getMessage());
        }
        return result;
    }
}