<template>
  <div class="help-container">
    <el-card class="help-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>📖 操作指南</span>
          <el-tag type="info">版本 1.0</el-tag>
        </div>
      </template>

      <el-collapse v-model="activeNames">
        <!-- 1. 系统概述 -->
        <el-collapse-item title="1. 系统概述" name="1">
          <div class="help-content">
            <p>农产品仓储管理系统是一个面向中小型农业企业的轻量级智能仓储管理平台。</p>
            <p><strong>核心功能：</strong></p>
            <ul>
              <li>农产品信息管理</li>
              <li>货位管理与剩余容量监控</li>
              <li>智能货位分配入库</li>
              <li>先进先出（FIFO）出库</li>
              <li>保质期预警、库存预警、环境预警</li>
              <li>环境温湿度监测</li>
              <li>操作日志审计</li>
              <li>数据统计报表</li>
            </ul>
          </div>
        </el-collapse-item>

        <!-- 2. 角色说明 -->
        <el-collapse-item title="2. 角色说明" name="2">
          <div class="help-content">
            <el-table :data="roles" border style="width: 100%">
              <el-table-column prop="role" label="角色" width="150" />
              <el-table-column prop="description" label="说明" />
              <el-table-column prop="permissions" label="权限范围" />
            </el-table>
            <p class="help-tip">💡 提示：不同角色登录后看到的菜单不同，权限已由系统控制。</p>
          </div>
        </el-collapse-item>

        <!-- 3. 登录与注册 -->
        <el-collapse-item title="3. 登录与注册" name="3">
          <div class="help-content">
            <h4>登录</h4>
            <p>1. 访问系统首页，进入登录页面</p>
            <p>2. 输入用户名和密码</p>
            <p>3. 点击"登录"按钮</p>
            <el-divider />
            <h4>测试账号</h4>
            <el-table :data="testAccounts" border style="width: 100%">
              <el-table-column prop="username" label="用户名" width="150" />
              <el-table-column prop="password" label="密码" width="150" />
              <el-table-column prop="role" label="角色" width="150" />
              <el-table-column prop="description" label="说明" />
            </el-table>
            <h4>注册新账号</h4>
            <p>1. 点击登录页的"注册"标签</p>
            <p>2. 填写用户名、密码等信息</p>
            <p>3. 点击"注册"按钮</p>
            <p>4. 注册成功后使用新账号登录</p>
            <p class="help-tip">💡 提示：注册的用户默认为"客户"角色，仅拥有查看权限。</p>
          </div>
        </el-collapse-item>

        <!-- 4. 农产品管理 -->
        <el-collapse-item title="4. 农产品管理" name="4">
          <div class="help-content">
            <h4>查看农产品列表</h4>
            <p>进入"农产品管理"页面，可查看所有农产品信息。</p>
            <h4>新增农产品</h4>
            <p>1. 点击"新增农产品"按钮</p>
            <p>2. 填写商品名称、分类、存储条件、保质期、预警阈值等信息</p>
            <p>3. 点击"确定"保存</p>
            <h4>编辑农产品</h4>
            <p>1. 点击对应行"编辑"按钮</p>
            <p>2. 修改信息后点击"确定"</p>
            <h4>删除农产品</h4>
            <p>1. 点击对应行"删除"按钮</p>
            <p>2. 确认删除</p>
            <h4>搜索与筛选</h4>
            <p>• 按商品名称模糊搜索</p>
            <p>• 按分类筛选</p>
            <p class="help-tip">💡 提示：预警阈值用于保质期预警，剩余天数 ≤ 阈值时触发预警。</p>
          </div>
        </el-collapse-item>

        <!-- 5. 货位管理 -->
        <el-collapse-item title="5. 货位管理" name="5">
          <div class="help-content">
            <h4>查看货位列表</h4>
            <p>进入"货位管理"页面，可查看所有货位信息，包括剩余容量进度条。</p>
            <h4>新增货位</h4>
            <p>1. 点击"新增货位"按钮</p>
            <p>2. 填写货位编号、区域、容量</p>
            <p>3. 点击"确定"保存</p>
            <h4>货位编号规则</h4>
            <p>• 格式：区域-排-层，如 A-01-01</p>
            <p>• A区/B区：常温区</p>
            <p>• 冷藏区：需要冷藏的商品</p>
            <p>• 冷冻区：需要冷冻的商品</p>
            <h4>剩余容量</h4>
            <p>• 系统自动计算：总容量 - 当前占用 = 剩余容量</p>
            <p>• 进度条颜色：绿色（充足）、橙色（较满）、红色（接近满）</p>
          </div>
        </el-collapse-item>

        <!-- 6. 入库管理 -->
        <el-collapse-item title="6. 入库管理" name="6">
          <div class="help-content">
            <h4>智能货位分配</h4>
            <p>1. 选择商品、供应商、入库数量、过期日期</p>
            <p>2. 系统自动推荐最佳货位并显示评分</p>
            <p>3. 确认后点击"执行入库"</p>
            <h4>货位分配算法</h4>
            <p>系统采用加权评分法，综合考虑以下因素：</p>
            <ul>
              <li>相同商品优先（100分）</li>
              <li>相同品类优先（80分）</li>
              <li>存储条件匹配（60分）</li>
              <li>靠近出口（40分）</li>
              <li>低货位优先（30分）</li>
              <li>容量充足（20分）</li>
            </ul>
            <h4>查看入库记录</h4>
            <p>入库记录列表显示所有入库历史，支持按商品查询。</p>
          </div>
        </el-collapse-item>

        <!-- 7. 出库管理 -->
        <el-collapse-item title="7. 出库管理" name="7">
          <div class="help-content">
            <h4>先进先出出库</h4>
            <p>1. 选择商品</p>
            <p>2. 系统自动按过期时间排序显示库存批次</p>
            <p>3. 输入出库数量</p>
            <p>4. 点击"执行出库"</p>
            <h4>出库规则</h4>
            <p>• 优先出库临近过期的商品</p>
            <p>• 若某批次全部出库，自动释放对应货位</p>
            <h4>查看出库记录</h4>
            <p>出库记录列表显示所有出库历史。</p>
          </div>
        </el-collapse-item>

        <!-- 8. 预警中心 -->
        <el-collapse-item title="8. 预警中心" name="8">
          <div class="help-content">
            <h4>保质期预警</h4>
            <p>• 为每种商品独立设置预警阈值</p>
            <p>• 剩余天数 ≤ 阈值时触发预警</p>
            <p>• 支持按紧急程度排序</p>
            <h4>库存预警</h4>
            <p>• 设置库存下限阈值</p>
            <p>• 库存低于阈值时触发补货提醒</p>
            <h4>环境预警</h4>
            <p>• 实时监测仓库温湿度</p>
            <p>• 超出正常范围时触发预警</p>
            <p>• 支持不同区域独立阈值</p>
          </div>
        </el-collapse-item>

        <!-- 9. 环境监测 -->
        <el-collapse-item title="9. 环境监测" name="9">
          <div class="help-content">
            <h4>查看环境数据</h4>
            <p>进入"环境监测"页面，可查看各区域的温湿度数据。</p>
            <h4>环境预警</h4>
            <p>• 温度/湿度超出阈值时显示预警</p>
            <p>• 预警级别：正常 → 警告 → 严重</p>
            <h4>阈值配置（管理员）</h4>
            <p>可为不同区域设置独立的温湿度阈值。</p>
          </div>
        </el-collapse-item>

        <!-- 10. 统计报表 -->
        <el-collapse-item title="10. 统计报表" name="10">
          <div class="help-content">
            <h4>入库统计报表</h4>
            <p>• 按日期查看入库趋势（折线图）</p>
            <p>• 按商品统计入库占比（饼图）</p>
            <p>• 按供应商统计入库占比</p>
            <h4>出库统计报表</h4>
            <p>• 按日期查看出库趋势</p>
            <p>• 按商品统计出库占比</p>
            <h4>损耗统计报表</h4>
            <p>• 统计过期、损坏等损耗情况</p>
            <p>• 按商品、按类型统计损耗</p>
          </div>
        </el-collapse-item>

        <!-- 11. 系统管理 -->
        <el-collapse-item title="11. 系统管理" name="11">
          <div class="help-content">
            <h4>用户管理</h4>
            <p>• 查看所有用户</p>
            <p>• 修改用户角色（管理员/仓库管理员/客户）</p>
            <p>• 删除用户</p>
            <h4>供应商管理</h4>
            <p>• 供应商信息增删改查</p>
            <p>• 按分类筛选供应商</p>
            <h4>客户管理</h4>
            <p>• 查看客户列表</p>
            <p>• 禁用/启用客户账户</p>
            <h4>操作日志</h4>
            <p>• 查看所有用户操作记录</p>
            <p>• 支持按用户、操作类型、时间范围查询</p>
            <h4>系统设置</h4>
            <p>• 配置预警阈值</p>
            <p>• 配置系统参数</p>
            <h4>数据备份</h4>
            <p>• 备份数据库</p>
            <p>• 下载/删除备份文件</p>
          </div>
        </el-collapse-item>

        <!-- 12. 常见问题 -->
        <el-collapse-item title="12. 常见问题" name="12">
          <div class="help-content">
            <h4>Q1: 忘记密码怎么办？</h4>
            <p>A: 请联系系统管理员重置密码。</p>

            <h4>Q2: 为什么无法删除货位？</h4>
            <p>A: 货位被占用时无法删除，请先清空该货位的货物。</p>

            <h4>Q3: 入库时没有推荐货位？</h4>
            <p>A: 请检查是否有空闲货位，或该商品的存储条件是否有匹配的区域。</p>

            <h4>Q4: 如何修改个人密码？</h4>
            <p>A: 点击右上角头像 → 个人中心 → 修改密码。</p>

            <h4>Q5: 报表没有数据？</h4>
            <p>A: 请先进行入库/出库操作，系统会根据实际数据生成报表。</p>

            <h4>Q6: 环境监测数据不更新？</h4>
            <p>A: 系统每5分钟自动模拟一次传感器数据，请稍后刷新。</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeNames = ref(['1'])

// 角色数据
const roles = [
  { role: '系统管理员', description: '系统最高权限管理者', permissions: '全部功能' },
  { role: '仓库管理员', description: '日常仓储作业人员', permissions: '业务操作权限（不含用户管理、日志）' },
  { role: '客户', description: '查看数据的客户', permissions: '仅查看权限（不含入库/出库操作）' }
]

// 测试账号
const testAccounts = [
  { username: 'admin', password: '123456', role: '系统管理员', description: '拥有全部权限' },
  { username: 'warehouse', password: '123456', role: '仓库管理员', description: '可进行入库/出库操作' },
  { username: 'customer', password: '123456', role: '客户', description: '仅可查看数据' }
]
</script>

<style scoped>
.help-container {
  padding: 0;
}

.help-card {
  border-radius: 12px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
}

.help-content {
  padding: 10px 0;
}

.help-content h4 {
  margin: 15px 0 10px 0;
  color: #409eff;
}

.help-content p {
  margin: 8px 0;
  line-height: 1.6;
  color: #333;
}

.help-content ul {
  margin: 8px 0;
  padding-left: 20px;
}

.help-content li {
  margin: 5px 0;
  line-height: 1.6;
}

.help-tip {
  margin-top: 15px;
  padding: 10px;
  background: #ecf5ff;
  border-radius: 6px;
  color: #409eff;
}

:deep(.el-collapse-item__header) {
  font-weight: bold;
  font-size: 16px;
}

:deep(.el-collapse-item__wrap) {
  padding: 0 20px;
}
</style>