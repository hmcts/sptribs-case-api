/// <reference types='codeceptjs' />
type steps_file = typeof import('./e2e/custom_steps.ts');
type PlaywrightHelpers = import('./e2e/helpers/browser_helper.ts');

declare namespace CodeceptJS {
  interface SupportObject { I: CodeceptJS.I }
  interface CallbackOrder { [0]: CodeceptJS.I }
  interface Methods extends CodeceptJS.Playwright, PlaywrightHelpers {}
  interface I extends ReturnType<steps_file> {}
  
  namespace Translation {
    interface Actions {}
  }
}