/**
 * 管理端用户状态管理（Pinia）
 * - 维护当前登录管理员信息、菜单、权限
 * - 提供获取信息、清除登录态、权限校验方法
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getMe } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 当前登录用户信息
  const info = ref(null)
  // 当前用户菜单树
  const menus = ref([])
  // 当前用户权限标识列表
  const permissions = ref([])

  /**
   * 获取当前用户信息并同步到状态
   * @returns {Promise<Object>} 用户信息
   */
  async function fetchInfo() {
    const data = await getMe()
    info.value = data
    menus.value = data.menus || []
    permissions.value = data.permissions || []
    return data
  }

  /**
   * 清除用户登录态与本地 token
   */
  function clear() {
    info.value = null
    menus.value = []
    permissions.value = []
    localStorage.removeItem('adminToken')
  }

  /**
   * 判断当前用户是否拥有指定权限
   * @param {string} perm 权限标识
   * @returns {boolean}
   */
  function hasPermission(perm) {
    if (info.value?.superAdmin === 1) return true
    return permissions.value.includes(perm)
  }

  return { info, menus, permissions, fetchInfo, clear, hasPermission }
})
