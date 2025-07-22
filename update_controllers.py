#!/usr/bin/env python3
"""
批量更新控制器脚本
移除所有控制器对BaseController的继承，改为使用ControllerUtils
"""

import os
import re

def update_controller_file(file_path):
    """更新单个控制器文件"""
    print(f"正在更新: {file_path}")
    
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # 1. 添加ControllerUtils导入
    if 'import cloud.compan.servlet.utils.ControllerUtils;' not in content:
        # 找到最后一个import语句
        import_pattern = r'(import.*?;)\s*\n\s*\n'
        match = re.search(import_pattern, content, re.DOTALL)
        if match:
            last_import = match.group(1)
            controller_utils_import = 'import cloud.compan.servlet.utils.ControllerUtils;'
            content = content.replace(last_import, last_import + '\n' + controller_utils_import)
    
    # 2. 移除extends BaseController
    content = re.sub(r'extends BaseController', '', content)
    
    # 3. 更新方法调用
    # success() -> ControllerUtils.success()
    content = re.sub(r'\bsuccess\(', 'ControllerUtils.success(', content)
    
    # error() -> ControllerUtils.error()
    content = re.sub(r'\berror\(', 'ControllerUtils.error(', content)
    
    # created() -> ControllerUtils.created()
    content = re.sub(r'\bcreated\(', 'ControllerUtils.created(', content)
    
    # handleServiceResult() -> ControllerUtils.handleServiceResult()
    content = re.sub(r'\bhandleServiceResult\(', 'ControllerUtils.handleServiceResult(', content)
    
    # handlePageResult() -> ControllerUtils.handlePageResult()
    content = re.sub(r'\bhandlePageResult\(', 'ControllerUtils.handlePageResult(', content)
    
    # handleCreateResult() -> ControllerUtils.handleCreateResult()
    content = re.sub(r'\bhandleCreateResult\(', 'ControllerUtils.handleCreateResult(', content)
    
    # validationError() -> ControllerUtils.validationError()
    content = re.sub(r'\bvalidationError\(', 'ControllerUtils.validationError(', content)
    
    # parseSearchCriteria() -> ControllerUtils.parseSearchCriteria()
    content = re.sub(r'\bparseSearchCriteria\(', 'ControllerUtils.parseSearchCriteria(', content)
    
    # parseIntParam() -> ControllerUtils.parseIntParam()
    content = re.sub(r'\bparseIntParam\(', 'ControllerUtils.parseIntParam(', content)
    
    # parseLongParam() -> ControllerUtils.parseLongParam()
    content = re.sub(r'\bparseLongParam\(', 'ControllerUtils.parseLongParam(', content)
    
    # parseBooleanParam() -> ControllerUtils.parseBooleanParam()
    content = re.sub(r'\bparseBooleanParam\(', 'ControllerUtils.parseBooleanParam(', content)
    
    # requireNonNull() -> ControllerUtils.requireNonNull()
    content = re.sub(r'\brequireNonNull\(', 'ControllerUtils.requireNonNull(', content)
    
    # requireNonEmpty() -> ControllerUtils.requireNonEmpty()
    content = re.sub(r'\brequireNonEmpty\(', 'ControllerUtils.requireNonEmpty(', content)
    
    # validateStringLength() -> ControllerUtils.validateStringLength()
    content = re.sub(r'\bvalidateStringLength\(', 'ControllerUtils.validateStringLength(', content)
    
    # validateEmail() -> ControllerUtils.validateEmail()
    content = re.sub(r'\bvalidateEmail\(', 'ControllerUtils.validateEmail(', content)
    
    # getClientIP() -> ControllerUtils.getClientIP()
    content = re.sub(r'\bgetClientIP\(', 'ControllerUtils.getClientIP(', content)
    
    # getUserAgent() -> ControllerUtils.getUserAgent()
    content = re.sub(r'\bgetUserAgent\(', 'ControllerUtils.getUserAgent(', content)
    
    # extractTokenFromRequest() -> ControllerUtils.extractTokenFromRequest()
    content = re.sub(r'\bextractTokenFromRequest\(', 'ControllerUtils.extractTokenFromRequest(', content)
    
    # 4. 删除私有方法
    # 删除extractTokenFromRequest私有方法
    content = re.sub(r'\s*private String extractTokenFromRequest\(HttpServletRequest request\)\s*\{[^}]*\}', '', content)
    
    # 删除getUserIdFromToken私有方法（如果只是简单的token提取）
    content = re.sub(r'\s*private Long getUserIdFromToken\(HttpServletRequest request\)\s*\{[^}]*\}', '', content)
    
    # 5. 更新注释
    content = re.sub(r'继承BaseController，', '', content)
    content = re.sub(r'Inherits BaseController, ', '', content)
    
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    print(f"✅ 更新完成: {file_path}")

def main():
    """主函数"""
    controller_dir = "src/main/java/cloud/compan/servlet/controller"
    
    # 需要更新的控制器文件
    controllers = [
        "FileController.java",
        "FolderController.java", 
        "StorageController.java",
        "NotificationController.java",
        "RecycleBinController.java",
        "ShareController.java"
    ]
    
    for controller in controllers:
        file_path = os.path.join(controller_dir, controller)
        if os.path.exists(file_path):
            update_controller_file(file_path)
        else:
            print(f"⚠️  文件不存在: {file_path}")

if __name__ == "__main__":
    main() 