<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { HomeFilled, Setting, Flag } from '@element-plus/icons-vue'
</script>

            <el-menu-item index="/dashboard">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-menu-item>
            
            <el-menu-item index="/keywords">
              <el-icon><Flag /></el-icon>
              <span>关键词生成</span>
            </el-menu-item> 