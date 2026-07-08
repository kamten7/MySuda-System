import { ref, reactive, type Ref } from 'vue'

export interface PaginationState {
  page: Ref<number>
  pageSize: Ref<number>
  total: Ref<number>
}

export function usePagination(
  fetchFn: () => Promise<void>,
  defaultPageSize = 10,
): PaginationState & {
  handlePageChange: (page: number) => void
  handleSizeChange: (size: number) => void
  reset: () => void
} {
  const page = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  function handlePageChange(p: number) {
    page.value = p
    fetchFn()
  }

  function handleSizeChange(s: number) {
    pageSize.value = s
    page.value = 1
    fetchFn()
  }

  function reset() {
    page.value = 1
    fetchFn()
  }

  return {
    page,
    pageSize,
    total,
    handlePageChange,
    handleSizeChange,
    reset,
  }
}
