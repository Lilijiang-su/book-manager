import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue')
    },
    {
      path: '/admin',
      component: () => import('../views/admin/Layout.vue'),
      meta: { role: 'admin' },
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/admin/Dashboard.vue')
        },
        {
          path: 'books',
          name: 'AdminBooks',
          component: () => import('../views/admin/BookManage.vue')
        },
        {
          path: 'users',
          name: 'AdminUsers',
          component: () => import('../views/admin/UserManage.vue')
        },
        {
          path: 'borrows',
          name: 'AdminBorrows',
          component: () => import('../views/admin/BorrowManage.vue')
        },
        {
          path: 'categories',
          name: 'AdminCategories',
          component: () => import('../views/admin/CategoryManage.vue')
        },
        {
          path: 'audit',
          name: 'AdminAudit',
          component: () => import('../views/admin/AuditLog.vue')
        }
      ]
    },
    {
      path: '/user',
      component: () => import('../views/user/Layout.vue'),
      meta: { role: 'user' },
      redirect: '/user/books',
      children: [
        {
          path: 'books',
          name: 'UserBooks',
          component: () => import('../views/user/BookList.vue')
        },
        {
          path: 'my-borrows',
          name: 'MyBorrows',
          component: () => import('../views/user/MyBorrows.vue')
        },
        {
          path: 'profile',
          name: 'UserProfile',
          component: () => import('../views/user/Profile.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.role && to.meta.role !== role) {
    next('/login')
    return
  }
  if ((to.path.startsWith('/admin') || to.path.startsWith('/user')) && !token) {
    next('/login')
    return
  }
  if ((to.path === '/login' || to.path === '/register') && token) {
    next(role === 'admin' ? '/admin' : '/user')
    return
  }
  next()
})

export default router
