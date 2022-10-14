/// <reference types='codeceptjs' />
type steps_file = typeof import('./e2e/steps_file.ts');

declare namespace CodeceptJS {
  interface SupportObject { I: CodeceptJS.I }
  interface CallbackOrder { [0]: CodeceptJS.I }
  interface Methods extends CodeceptJS.Playwright {}
  interface I extends ReturnType<steps_file> {}
  
  namespace Translation {
    interface Actions {}
  }
}