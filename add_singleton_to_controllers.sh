#!/bin/bash

# 为所有Controller添加@Singleton注解
CONTROLLER_FILES=(
    "src/main/java/cloud/compan/servlet/controller/AclController.java"
    "src/main/java/cloud/compan/servlet/controller/AuthController.java"
    "src/main/java/cloud/compan/servlet/controller/DemoController.java"
    "src/main/java/cloud/compan/servlet/controller/FileController.java"
    "src/main/java/cloud/compan/servlet/controller/FolderController.java"
    "src/main/java/cloud/compan/servlet/controller/NotificationController.java"
    "src/main/java/cloud/compan/servlet/controller/RecycleBinController.java"
    "src/main/java/cloud/compan/servlet/controller/ShareController.java"
    "src/main/java/cloud/compan/servlet/controller/StorageController.java"
    "src/main/java/cloud/compan/servlet/controller/TestController.java"
)

for file in "${CONTROLLER_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "Processing $file"
        # 添加Singleton导入
        sed -i 's/import com.google.inject.Inject;/import com.google.inject.Inject;\nimport com.google.inject.Singleton;/' "$file"
        # 添加@Singleton注解到类声明
        sed -i 's/@Controller/@Controller\n@Singleton/' "$file"
    fi
done

echo "All controllers updated with @Singleton annotation" 