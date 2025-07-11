package com.zh8888.controller;

import com.zh8888.model.Menu;
import com.zh8888.model.User;
import com.zh8888.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单控制器
 */
@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 菜单主页面
     */
    @GetMapping("")
    public String menuIndex(HttpSession session, Model model) {
        // 检查用户登录状态
        User user = (User) session.getAttribute("user");
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("isLoggedIn", true);
        } else {
            model.addAttribute("isLoggedIn", false);
        }

        // 获取所有分类的菜品
        Map<String, List<Menu>> categorizedMenus = menuService.getMenusByCategories();
        model.addAttribute("categorizedMenus", categorizedMenus);

        // 获取推荐菜品
        List<Menu> recommendedMenus = menuService.getRecommendedMenus();
        model.addAttribute("recommendedMenus", recommendedMenus);

        // 设置页面元信息
        model.addAttribute("pageTitle", "菜单 - 随心点");
        model.addAttribute("pageDescription", "浏览我们精心准备的美味菜品，丰富选择等您品尝");
        model.addAttribute("pageType", "public");

        return "public/menu";
    }

    /**
     * 根据分类获取菜品 (AJAX接口)
     */
    @GetMapping("/category/{category}")
    @ResponseBody
    public Map<String, Object> getMenusByCategory(@PathVariable String category) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Menu> menus = menuService.getMenusByCategory(category);
            response.put("success", true);
            response.put("data", menus);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取菜品失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取菜品详情 (AJAX接口)
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public Map<String, Object> getMenuDetail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Menu menu = menuService.getMenuById(id);
            if (menu != null) {
                response.put("success", true);
                response.put("data", menu);
                response.put("message", "获取成功");
            } else {
                response.put("success", false);
                response.put("message", "菜品不存在");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取菜品详情失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 搜索菜品 (AJAX接口)
     */
    @GetMapping("/search")
    @ResponseBody
    public Map<String, Object> searchMenus(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 先按名称搜索
            List<Menu> nameResults = menuService.searchMenusByName(keyword);
            
            // 再按标签搜索
            List<Menu> tagResults = menuService.searchMenusByTag(keyword);
            
            // 合并结果，避免重复
            Map<Integer, Menu> uniqueMenus = new HashMap<>();
            nameResults.forEach(menu -> uniqueMenus.put(menu.getId(), menu));
            tagResults.forEach(menu -> uniqueMenus.put(menu.getId(), menu));
            
            response.put("success", true);
            response.put("data", uniqueMenus.values());
            response.put("message", "搜索完成");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "搜索失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取推荐菜品 (AJAX接口)
     */
    @GetMapping("/recommended")
    @ResponseBody
    public Map<String, Object> getRecommendedMenus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Menu> recommendedMenus = menuService.getRecommendedMenus();
            response.put("success", true);
            response.put("data", recommendedMenus);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取推荐菜品失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取素食菜品 (AJAX接口)
     */
    @GetMapping("/vegetarian")
    @ResponseBody
    public Map<String, Object> getVegetarianMenus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Menu> vegetarianMenus = menuService.getVegetarianMenus();
            response.put("success", true);
            response.put("data", vegetarianMenus);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取素食菜品失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 获取所有分类菜品 (AJAX接口)
     */
    @GetMapping("/all")
    @ResponseBody
    public Map<String, Object> getAllCategorizedMenus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, List<Menu>> categorizedMenus = menuService.getMenusByCategories();
            response.put("success", true);
            response.put("data", categorizedMenus);
            response.put("message", "获取成功");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "获取菜品失败：" + e.getMessage());
        }
        
        return response;
    }

    /**
     * 管理员菜单页面
     */
    @GetMapping("/admin")
    public String adminMenu(HttpSession session, Model model) {
        // 检查用户登录状态及权限
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }
        
        if (user.getRole() != 1) { // 不是管理员
            return "redirect:/user/dashboard";
        }

        // 获取所有菜品
        List<Menu> allMenus = menuService.getAllMenus();
        model.addAttribute("menus", allMenus);
        model.addAttribute("user", user);
        
        return "admin/menu";
    }
    
    /**
     * 添加新菜品
     */
    @PostMapping("/admin/add")
    @ResponseBody
    public Map<String, Object> addMenu(@RequestBody Menu menu, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户登录状态及权限
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            response.put("success", false);
            response.put("message", "无权限操作");
            return response;
        }
        
        try {
            // 生成新的菜品编号
            String menuNo = generateMenuNo(menu.getTags());
            menu.setMenuNo(menuNo);
            
            boolean result = menuService.addMenu(menu);
            if (result) {
                response.put("success", true);
                response.put("message", "添加菜品成功");
            } else {
                response.put("success", false);
                response.put("message", "添加菜品失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "添加菜品时发生错误：" + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 根据菜品分类生成编号
     */
    private String generateMenuNo(String tags) {
        String prefix = "M"; // 默认前缀
        
        if (tags != null) {
            if (tags.contains("热菜")) {
                prefix = "H"; // 热菜 - Hot
            } else if (tags.contains("凉菜")) {
                prefix = "L"; // 凉菜 - Liang
            } else if (tags.contains("汤类")) {
                prefix = "T"; // 汤类 - Tang
            } else if (tags.contains("主食")) {
                prefix = "M"; // 主食 - Main
            } else if (tags.contains("饮品")) {
                prefix = "D"; // 饮品 - Drink
            }
        }
        
        // 获取该分类下的最大编号
        int nextNumber = menuService.getNextMenuNumber(prefix);
        
        return prefix + String.format("%03d", nextNumber);
    }
    
    /**
     * 更新菜品信息
     */
    @PostMapping("/admin/update")
    @ResponseBody
    public Map<String, Object> updateMenu(@RequestBody Menu menu, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户登录状态及权限
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            response.put("success", false);
            response.put("message", "无权限操作");
            return response;
        }
        
        try {
            boolean result = menuService.updateMenu(menu);
            if (result) {
                response.put("success", true);
                response.put("message", "更新菜品成功");
            } else {
                response.put("success", false);
                response.put("message", "更新菜品失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "更新菜品时发生错误：" + e.getMessage());
        }
        
        return response;
    }
    
    /**
     * 删除菜品
     */
    @DeleteMapping("/admin/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteMenu(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        // 检查用户登录状态及权限
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != 1) {
            response.put("success", false);
            response.put("message", "无权限操作");
            return response;
        }
        
        try {
            boolean result = menuService.deleteMenu(id);
            if (result) {
                response.put("success", true);
                response.put("message", "删除菜品成功");
            } else {
                response.put("success", false);
                response.put("message", "删除菜品失败");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "删除菜品时发生错误：" + e.getMessage());
        }
        
        return response;
    }

}

   