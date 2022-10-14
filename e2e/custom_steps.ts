import loginPage from './features/home/pages/login';
import config from './config';
// import output from 'codeceptjs';

const SIGNED_IN_SELECTOR = 'exui-header';
const PASSWORD = '';

export default async () => {
    return actor({
        async login(user) {
            if (await this.hasSelector(SIGNED_IN_SELECTOR)) {
                await this.signOut();
            }
      
            await this.retryUntilExists(async () => {
                this.amOnPage(config.url.local, 90);
                console.log(`Signing in user: ${user.type}`);
                loginPage.login(user, PASSWORD);
            }, SIGNED_IN_SELECTOR);
          },
    });
}