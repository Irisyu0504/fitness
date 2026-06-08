import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import test from 'node:test'

const viewSource = readFileSync(new URL('../src/views/BodyRecordView.vue', import.meta.url), 'utf8')
const chartSource = readFileSync(new URL('../src/components/body/BodyTrendChart.vue', import.meta.url), 'utf8')

test('body record view prepares joint BMI waistline weight body fat trend series', () => {
  assert.match(viewSource, /const trendMetrics = \[/)
  assert.match(viewSource, /key: 'weight', label: '体重', unit: 'kg'/)
  assert.match(viewSource, /key: 'waistline', label: '腰围', unit: 'cm'/)
  assert.match(viewSource, /key: 'bmi', label: 'BMI', unit: ''/)
  assert.match(viewSource, /key: 'bodyFatRate', label: '体脂率', unit: '%'/)
  assert.match(viewSource, /series: trendMetrics\.map/)
  assert.match(viewSource, /aggregateMetricAverages/)
  assert.doesNotMatch(viewSource, /values: data\.map\(d => d\[activeMetric\.value\]\)/)
})

test('body trend chart renders all body metrics as a joined multi-series line chart', () => {
  assert.match(chartSource, /身体数据联合趋势/)
  assert.match(chartSource, /props\.chartData\.series/)
  assert.match(chartSource, /legend:/)
  assert.match(chartSource, /yAxis:\s*\[/)
  assert.match(chartSource, /yAxisIndex/)
  assert.match(chartSource, /connectNulls: true/)
  assert.match(chartSource, /item\.key === props\.activeMetric/)
})
