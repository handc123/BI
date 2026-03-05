# 表结构分析：t_cbis_t_daikuan_hzwdb（贷款汇总维度表）

## 表概述
- **表名**: t_cbis_t_daikuan_hzwdb
- **用途**: 贷款业务汇总维度表，用于贷款数据的多维度分析
- **字段总数**: 145个字段
- **数据库**: ry-vue

## 字段分类

### 1. 维度字段（用于分组、筛选）- 共约80个

#### 基础维度
- `id` - ID
- `load_date` - 数据加载日期（重要时间维度）
- `qydm` - 区域代码
- `sjbsjgid` - 数据报送机构id

#### 贷款分类维度
- `dklx` - 贷款类型
- `dkztlx` - 贷款主体类型
- `wjfl` - 五级分类（重要风险维度）
- `dkzt` - 贷款状态
- `dkqxlx` - 贷款期限类型
- `dkfflx` - 贷款发放类型
- `cplb` - 产品类别

#### 地区维度
- `sxyhssdq` - 授信银行所属地区
- `dkztszdq` - 贷款主体所在地区

#### 行业维度
- `dktxhy` - 贷款投向行业
- `dkztsshy` - 贷款主体所属行业

#### 担保与风险维度
- `dbfs` - 担保方式
- `dkztkglx` - 贷款主体控股类型
- `jgcj` - 利率类型

#### 期限维度
- `yqqx` - 逾期期限
- `yqqxxf` - 逾期期限细分
- `syqx` - 剩余期限
- `syqxxf` - 剩余期限细分
- `dkhtqx` - 贷款合同期限
- `dkhtqxxf` - 贷款合同期限细分

#### 特殊标识维度（约40个布尔型标识）
- `yqbs` - 逾期标识
- `phxdklxbs` - 普惠型贷款类型标识
- `grjyxdkbs` - 个人经营性贷款标识
- `lsdkbs` - 绿色贷款标识
- `kjqybs` - 科技企业标识
- `sndkbs` - 涉农贷款标识
- `ytdkbs` - 银团贷款标识
- `dfzfrzptbs` - 地方政府融资平台标识
- `bgdkbs` - 并购贷款标识
- `hlwdkbs` - 互联网贷款标识
- `xhdbs` - 循环贷标识
- 等等...

### 2. 指标字段（用于计算、聚合）- 共约65个

#### 核心余额指标
- `jkye` (decimal 20,2) - 借款余额（最重要）
- `ybjkye` (decimal 20,2) - 原币借款余额
- `yqdk ye` (decimal 20,2) - 逾期贷款余额

#### 核心金额指标
- `jkje` (decimal 20,2) - 借款金额
- `ybjkje` (decimal 20,2) - 原币借款金额

#### 笔数指标
- `jkbs` (int) - 借款笔数
- `dtffbs` (int) - 当天发放笔数
- `dzffbs` (int) - 当周发放笔数
- `dyffbs` (int) - 当月发放笔数
- `djffbs` (int) - 当季发放笔数
- `dnffbs` (int) - 当年发放笔数

#### 发放金额指标（按时间周期）
- `dtffje` (decimal 20,2) - 当天发放金额
- `dzffje` (decimal 20,2) - 当周发放金额
- `dyffje` (decimal 20,2) - 当月发放金额
- `djffje` (decimal 20,2) - 当季发放金额
- `dnffje` (decimal 20,2) - 当年发放金额

#### 利息额指标
- `cldknhlxe` (decimal 20,6) - 存量贷款年化利息额
- `dtffdknhlxe` (decimal 20,6) - 当天发放贷款年化利息额
- `dzffdknhlxe` (decimal 20,6) - 当周发放贷款年化利息额
- `dyffdknhlxe` (decimal 20,6) - 当月发放贷款年化利息额
- `djffdknhlxe` (decimal 20,6) - 当季发放贷款年化利息额
- `dnffdknhlxe` (decimal 20,6) - 当年发放贷款年化利息额

#### 利率指标
- `drffdkzgll` (decimal 20,6) - 当日发放贷款最高利率
- `drffdkzdll` (decimal 20,6) - 当日发放贷款最低利率
- `dyffdkzgll` (decimal 20,6) - 当月发放贷款最高利率
- `dyffdkzdll` (decimal 20,6) - 当月发放贷款最低利率

#### 不良贷款处置指标（按时间周期和处置方式）
每个时间周期（天/周/月/季/年）都有7种处置方式：
1. 向上迁徙不良贷款金额
2. 现金处置不良金额
3. 转让处置不良金额
4. 向上级行划转不良贷款金额
5. 核销处置不良金额
6. 以物抵贷处置不良金额
7. 其他处置不良金额

#### 不良贷款新增指标
- `dtxzblje` (decimal 20,2) - 当天新增不良金额
- `dzxzblje` (decimal 20,2) - 当周新增不良金额
- `dyxzblje` (decimal 20,2) - 当月新增不良金额
- `djxzblje` (decimal 20,2) - 当季新增不良金额
- `dnxzblje` (decimal 20,2) - 当年新增不良金额

#### 本年发放不良指标
- `dtxzbnffblje` (decimal 20,2) - 当天新增本年发放不良金额
- `dzxzbnffblje` (decimal 20,2) - 当周新增本年发放不良金额
- `dyxzbnffblje` (decimal 20,2) - 当月新增本年发放不良金额
- `djxzbnffblje` (decimal 20,2) - 当季新增本年发放不良金额
- `dnxzbnffblje` (decimal 20,2) - 当年新增本年发放不良金额

#### 迁徙指标
- `dnblsqzcje` (decimal 20,2) - 当年不良上迁正常金额
- `dnzcxqblje` (decimal 20,2) - 当年正常下迁不良金额

#### 押品价值指标
- `jrlypjz` (decimal 20,2) - 金融类押品价值
- `yszklypjz` (decimal 20,2) - 应收账款类押品价值
- `fdclypjz` (decimal 20,2) - 房地产类押品价值
- `dclypjz` (decimal 20,2) - 动产类押品价值
- `qllypjz` (decimal 20,2) - 权利类押品价值
- `qtypjz` (decimal 20,2) - 其他押品价值

#### 其他重要指标
- `jzzb` (decimal 20,2) - 减值准备

## 关键索引字段
- `dkztlx` - 贷款主体类型（有索引）
- `wjfl` - 五级分类（有索引）
- `qydm` - 区域代码（有索引）
- `dkzt` - 贷款状态（有索引）
- `load_date` - 数据加载日期（有索引）

## 数据特点
1. **时间序列数据**: 通过 `load_date` 字段支持时间序列分析
2. **多维度分析**: 支持按地区、行业、产品、风险等级等多维度分析
3. **周期性指标**: 大量按天/周/月/季/年统计的指标
4. **风险管理**: 丰富的不良贷款和风险相关指标
5. **业务标识**: 约40个布尔型标识字段，支持精细化业务分类

## 推荐的IRAS配置

### 1. 数据集配置
```sql
-- 基础数据集
SELECT * FROM t_cbis_t_daikuan_hzwdb 
WHERE load_date = CURDATE()

-- 历史趋势数据集
SELECT * FROM t_cbis_t_daikuan_hzwdb 
WHERE load_date >= DATE_SUB(CURDATE(), INTERVAL 1 YEAR)
```

### 2. 推荐的基础指标

#### 规模类指标
1. **贷款余额总计** = SUM(jkye)
2. **贷款笔数总计** = SUM(jkbs)
3. **当月发放金额** = SUM(dyffje)
4. **当月发放笔数** = SUM(dyffbs)

#### 风险类指标
5. **不良贷款余额** = SUM(jkye) WHERE wjfl IN ('次级', '可疑', '损失')
6. **逾期贷款余额** = SUM(yqdk ye)
7. **当月新增不良** = SUM(dyxzblje)
8. **减值准备总额** = SUM(jzzb)

#### 收益类指标
9. **存量贷款利息收入** = SUM(cldknhlxe)
10. **当月新增贷款利息** = SUM(dyffdknhlxe)

### 3. 推荐的计算指标

#### 比率类指标
1. **不良贷款率** = 不良贷款余额 / 贷款余额总计 * 100
   - 条件比率类型
   - 分子条件: `wjfl IN ('次级', '可疑', '损失')`
   - 分母条件: `1=1`

2. **逾期贷款率** = 逾期贷款余额 / 贷款余额总计 * 100

3. **拨备覆盖率** = 减值准备总额 / 不良贷款余额 * 100

4. **平均贷款利率** = 存量贷款利息收入 / 贷款余额总计 * 100

#### 增长类指标
5. **贷款余额环比增长率** = (本期余额 - 上期余额) / 上期余额 * 100

### 4. 推荐的维度组合

#### 常用维度组合
1. **地区分析**: qydm + sxyhssdq + dkztszdq
2. **风险分析**: wjfl + dkzt + yqbs
3. **产品分析**: dklx + cplb + dkqxlx
4. **行业分析**: dktxhy + dkztsshy
5. **时间分析**: load_date（按日/月/季/年）

### 5. 推荐的仪表板

#### 仪表板1：贷款规模总览
- 贷款余额总计（指标卡）
- 贷款笔数总计（指标卡）
- 贷款余额趋势（折线图，按load_date）
- 贷款余额分布（饼图，按dklx）
- 地区贷款排名（柱状图，按qydm）

#### 仪表板2：风险监控
- 不良贷款率（仪表盘）
- 不良贷款余额（指标卡）
- 五级分类分布（饼图，按wjfl）
- 不良贷款趋势（折线图，按load_date）
- 逾期贷款分析（柱状图，按yqqx）

#### 仪表板3：业务分析
- 当月发放金额（指标卡）
- 发放金额趋势（折线图）
- 行业分布（柱状图，按dktxhy）
- 产品结构（饼图，按cplb）
- 期限结构（柱状图，按dkqxlx）

#### 仪表板4：收益分析
- 利息收入总计（指标卡）
- 平均贷款利率（指标卡）
- 利率区间分布（柱状图）
- 利息收入趋势（折线图）

## 使用建议

1. **数据刷新频率**: 建议每日更新，基于 `load_date` 字段
2. **数据保留期**: 建议保留至少2年历史数据用于趋势分析
3. **性能优化**: 
   - 在 `load_date` 上建立索引（已有）
   - 考虑按月份分区
   - 对常用查询创建物化视图
4. **数据质量**: 
   - 定期检查 `load_date` 的连续性
   - 监控关键指标的异常波动
   - 验证金额字段的合理性范围

## 下一步操作

1. 在IRAS系统中创建数据源（连接到 ry-vue 数据库）
2. 创建数据集（使用上述SQL）
3. 配置基础指标和计算指标
4. 创建仪表板并配置可视化组件
5. 设置数据刷新计划
