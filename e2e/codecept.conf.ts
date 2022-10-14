import { setHeadlessWhen, setCommonPlugins } from '@codeceptjs/configure';
import _config from './config'
// turn on headless mode when running with HEADLESS=true environment variable
// export HEADLESS=true && npx codeceptjs run
setHeadlessWhen(process.env.HEADLESS);

// enable all common plugins https://github.com/codeceptjs/configure#setcommonplugins
setCommonPlugins();

export const config: CodeceptJS.MainConfig = {
  ..._config,
  tests: './tests/*_tests.ts',
  output: './output',
  helpers: {
    Playwright: {
      url: 'http://localhost:3000',
      show: true,
      browser: 'chromium',
      waitForNavigation: 'networkidle0',
      waitForTimeout: 30000,
      waitForAction: 200
    }
  },
  include: {
    I: './steps_file.ts'
  },
  plugins: {
    autoDelay: {
      enabled: true,
      methods: [
        'click',
        'fillField',
        'checkOption',
        'selectOption',
        'attachFile',
      ],
    },
    retryFailedStep: {
      enabled: true,
    },
    screenshotOnFail: {
      enabled: true,
      fullPageScreenshots: true,
    },
  },  
  mocha: {
    bail: true,
    reporterOptions: {
      'codeceptjs-cli-reporter': {
        stdout: '-',
        options: {
          steps: false,
        },
      },
      'mocha-junit-reporter': {
        stdout: '-',
        options: {
          mochaFile: process.env.REPORT_FILE || 'test-results/functional/result.xml',
        },
      },
      'mochawesome': {
        stdout: '-',
        options: {
          reportDir: process.env.REPORT_DIR || 'test-results/functional',
          inlineAssets: true,
          json: false,
        },
      },
    }
  }
}